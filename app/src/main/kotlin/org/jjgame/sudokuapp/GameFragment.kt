package org.jjgame.sudokuapp

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jjgame.sudoku.CellIndex
import org.jjgame.sudoku.CellState
import org.jjgame.sudoku.SudokuCandidate
import org.jjgame.sudoku.SudokuGameSession
import org.jjgame.sudoku.SudokuCell
import org.jjgame.sudoku.SudokuPuzzle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GameFragment : Fragment() {

    companion object {
        private const val ARG_TRAINING_ASSET_PATH = "trainingAssetPath"
        private const val ARG_TRAINING_TECHNIQUE_NAME = "trainingTechniqueName"

        fun newTrainingInstance(
            assetPath: String,
            techniqueName: String,
        ) = GameFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TRAINING_ASSET_PATH, assetPath)
                putString(ARG_TRAINING_TECHNIQUE_NAME, techniqueName)
            }
        }
    }

    private sealed class ScreenMode {
        data object Normal : ScreenMode()

        data class Training(
            val assetPath: String,
            val techniqueName: String,
        ) : ScreenMode()
    }

    private lateinit var boardView: SudokuBoardView
    private lateinit var findCandidates: Button
    private lateinit var editCandidates: AppCompatImageButton
    private lateinit var statusText: TextView
    private lateinit var errorText: TextView
    private lateinit var timerText: TextView
    private lateinit var btnHome: Button
    private lateinit var btnClear: Button
    private lateinit var gameViewModel: SudokuGameViewModel
    private lateinit var digitButtons: List<Button>
    private lateinit var mode: ScreenMode

    private var timerJob: Job? = null
    private var trainingRecords: List<TrainingRecord> = emptyList()
    private var trainingIndex = 0
    private lateinit var trainingSession: SudokuGameSession

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boardView = view.findViewById(R.id.boardView)
        findCandidates = view.findViewById(R.id.btnFindCandidates)
        editCandidates = view.findViewById(R.id.btnToggleEditCandidates)
        statusText = view.findViewById(R.id.statusText)
        errorText = view.findViewById(R.id.errorText)
        timerText = view.findViewById(R.id.timerText)
        btnHome = view.findViewById(R.id.btnHome)
        btnClear = view.findViewById(R.id.btnClear)
        gameViewModel = ViewModelProvider(requireActivity())[SudokuGameViewModel::class.java]
        mode = buildModeFromArgs()

        val digitIds = listOf(
            R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6,
            R.id.btn7, R.id.btn8, R.id.btn9,
        )
        digitButtons = digitIds.map { id -> view.findViewById(id) }

        init()

        btnHome.setOnClickListener {
            persistIfNormal()
            parentFragmentManager.popBackStack()
        }

        findCandidates.setOnClickListener {
            currentSession().fastFillCandidate()
            persistIfNormal()
            updateCandidatesUi()
            updateNumberButtonsUi()
            boardView.invalidate()
        }

        editCandidates.setOnClickListener {
            editCandidates.isSelected = !editCandidates.isSelected
            updateEditCandidateToggleUi()
            boardView.setEditCandidateMode(editCandidates.isSelected)
        }

        digitButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                boardView.selectNumber(index + 1)
            }
        }

        btnClear.setOnClickListener {
            boardView.clearSelection()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mode is ScreenMode.Normal) startTimer()
    }

    override fun onPause() {
        super.onPause()
        if (mode is ScreenMode.Normal) {
            stopTimer()
            gameViewModel.persistGameSession()
        }
    }

    private fun startTimer() {
        if (gameViewModel.gameSession.finishedAt != null) return  // game already finished
        timerJob = viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                updateTimerUi()
                delay(1_000)
                gameViewModel.tickElapsed()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateTimerUi() {
        if (mode !is ScreenMode.Normal) return
        val totalSeconds = gameViewModel.gameSession.elapsedSeconds
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        timerText.text = "%d:%02d".format(minutes, seconds)
    }

    private fun init() {
        when (val m = mode) {
            is ScreenMode.Normal -> initNormalMode()
            is ScreenMode.Training -> initTrainingMode(m)
        }
    }

    private fun initNormalMode() {
        boardView.init(gameViewModel.gameSession) {
            gameViewModel.persistGameSession()
            updateNumberButtonsUi()
            updateErrorUi()
            checkWin()
        }
        boardView.setEditCandidateMode(editCandidates.isSelected)
        updateCandidatesUi()
        updateEditCandidateToggleUi()
        updateNumberButtonsUi()
        updateTimerUi()
        updateErrorUi()
        statusText.text = gameViewModel.currentDifficulty.name.lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    private fun initTrainingMode(training: ScreenMode.Training) {
        stopTimer()
        statusText.text = training.techniqueName
        timerText.text = "Loading..."
        errorText.text = "errors: 0"
        setInteractiveEnabled(false)

        val appCtx = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                TrainingRepository.loadSetRecords(appCtx, training.assetPath).shuffled()
            }
            if (!isAdded) return@launch
            trainingRecords = loaded
            if (trainingRecords.isEmpty()) {
                showNoSamplesDialog()
                return@launch
            }
            loadTrainingPuzzle(0)
        }
    }

    private fun loadTrainingPuzzle(index: Int) {
        val training = mode as? ScreenMode.Training ?: return
        val record = trainingRecords[index]
        trainingIndex = index

        val session = SudokuGameSession(
            puzzle = buildPuzzleFromRecord(record),
            candidates = SudokuCandidate(),
        )
        trainingSession = session
        setInteractiveEnabled(true)

        boardView.init(session) {
            updateNumberButtonsUi()
            updateErrorUi()
            checkTrainingSuccess(session, record)
        }
        boardView.setEditCandidateMode(editCandidates.isSelected)
        updateCandidatesUi()
        updateEditCandidateToggleUi()
        updateNumberButtonsUi()
        updateErrorUi()
        statusText.text = training.techniqueName
        timerText.text = "${trainingIndex + 1}/${trainingRecords.size}"
        boardView.invalidate()
    }

    private fun updateCandidatesUi() {
        editCandidates.isEnabled = true
        boardView.setEditCandidateMode(editCandidates.isSelected)
    }

    private fun updateErrorUi() {
        val count = currentSession().errorCount
        errorText.text = "errors: $count"
        errorText.setTextColor(
            when {
                count == 0 -> "#888888"
                count == 1 -> "#F9A825"  // yellow
                count == 2 -> "#EF6C00"  // orange
                else       -> "#C62828"  // red
            }.toColorInt()
        )
    }

    private fun updateEditCandidateToggleUi() {
        val active = editCandidates.isSelected
        editCandidates.imageTintList = ColorStateList.valueOf(
            if (active) "#FB8C00".toColorInt() else "#555555".toColorInt(),
        )
        editCandidates.alpha = if (active) 1f else 0.7f
    }

    private fun updateNumberButtonsUi() {
        val remainingByValue = currentSession().puzzle.remainingCountsByValue()

        digitButtons.forEachIndexed { index, button ->
            val value = index + 1
            val remaining = remainingByValue[index]
            button.isAllCaps = false
            button.text = formatDigitButtonText(value, remaining)
            // Keep the slot occupied so other buttons do not shift when one is finished.
            button.visibility = if (remaining == 0) View.INVISIBLE else View.VISIBLE
        }

        autoSelectNextUnfinishedIfCurrentIsDone(remainingByValue)
    }

    private fun formatDigitButtonText(value: Int, remaining: Int): CharSequence {
        val text = "$value\n$remaining"
        val spannable = SpannableString(text)
        val secondLineStart = text.indexOf('\n') + 1
        val end = text.length

        spannable.setSpan(
            ForegroundColorSpan("#666666".toColorInt()),
            secondLineStart,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            StyleSpan(Typeface.NORMAL),
            secondLineStart,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        spannable.setSpan(
            RelativeSizeSpan(0.78f),
            secondLineStart,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
        )

        return spannable
    }

    private fun autoSelectNextUnfinishedIfCurrentIsDone(remainingByValue: IntArray) {
        val selected = currentSession().selectedNumber ?: return
        if (remainingByValue[selected - 1] > 0) return

        // Continue from selected+1, wrap after 9, and pick the first unfinished value.
        val next = (1..9)
            .map { step -> ((selected - 1 + step) % 9) + 1 }
            .firstOrNull { value -> remainingByValue[value - 1] > 0 }

        if (next == null) {
            boardView.clearSelection()
        } else {
            boardView.selectNumber(next)
        }
    }

    private fun checkWin() {
        if (!boardView.isSolved()) return

        stopTimer()
        gameViewModel.recordFinish()
        gameViewModel.clearSavedGame()

        val session = gameViewModel.gameSession
        val dtFormat = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())
        val startStr = dtFormat.format(Date(session.startedAt))
        val endStr = dtFormat.format(Date(session.finishedAt ?: System.currentTimeMillis()))
        val elapsed = session.elapsedSeconds
        val timeStr = if (elapsed >= 3600) {
            "%d:%02d:%02d".format(elapsed / 3600, (elapsed % 3600) / 60, elapsed % 60)
        } else {
            "%d:%02d".format(elapsed / 60, elapsed % 60)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("🎉 You Win!")
            .setMessage(
                "Congratulations, you solved the puzzle!\n\n" +
                        "⏱ Time taken:  $timeStr\n" +
                        "🕐 Started:      $startStr\n" +
                        "🏁 Finished:    $endStr\n" +
                        "❌ Errors:        ${session.errorCount}",
            )
            .setPositiveButton("New Game") { _, _ ->
                gameViewModel.startGame()
                init()
                startTimer()
            }
            .setNegativeButton("Home") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    private fun currentSession(): SudokuGameSession {
        return when (mode) {
            is ScreenMode.Normal -> gameViewModel.gameSession
            is ScreenMode.Training -> trainingSession
        }
    }

    private fun setInteractiveEnabled(enabled: Boolean) {
        boardView.isEnabled = enabled
        findCandidates.isEnabled = enabled
        editCandidates.isEnabled = enabled
        btnClear.isEnabled = enabled
        digitButtons.forEach { it.isEnabled = enabled }
    }

    private fun persistIfNormal() {
        if (mode is ScreenMode.Normal) {
            gameViewModel.persistGameSession()
        }
    }

    private fun buildModeFromArgs(): ScreenMode {
        val assetPath = arguments?.getString(ARG_TRAINING_ASSET_PATH)
        if (assetPath.isNullOrBlank()) {
            return ScreenMode.Normal
        }
        return ScreenMode.Training(
            assetPath = assetPath,
            techniqueName = arguments?.getString(ARG_TRAINING_TECHNIQUE_NAME) ?: "Training",
        )
    }

    private fun buildPuzzleFromRecord(record: TrainingRecord): SudokuPuzzle {
        val source = record.sourcePuzzle
        val before = record.puzzleBeforeStep
        val after = record.puzzleAfterStep

        return SudokuPuzzle(Array(81) { idx ->
            val sourceCh = source[idx]
            val beforeCh = before[idx]
            val afterCh = after[idx]

            when {
                sourceCh != '.' -> SudokuCell(value = sourceCh - '0', state = CellState.GIVEN)
                beforeCh != '.' -> SudokuCell(value = beforeCh - '0', state = CellState.FOUND)
                else -> {
                    val trueValue = if (afterCh != '.') afterCh - '0' else 0
                    SudokuCell(value = trueValue, state = CellState.NOT_FOUND)
                }
            }
        })
    }

    private fun checkTrainingSuccess(session: SudokuGameSession, record: TrainingRecord) {
        val targetCell = record.targetCell ?: return
        val targetValue = record.targetValue ?: return
        val cell = session.puzzle.cellAt(CellIndex(targetCell))
        if (cell.state == CellState.FOUND && cell.value == targetValue) {
            showTrainingSuccessDialog(record)
        }
    }

    private fun showTrainingSuccessDialog(record: TrainingRecord) {
        val hasNext = trainingIndex + 1 < trainingRecords.size
        AlertDialog.Builder(requireContext())
            .setTitle("✅ Correct!")
            .setMessage(
                "Well done!\n\n" +
                        "Technique: ${record.solutionType.replace('_', ' ')}\n" +
                        "Step: ${record.stepText}\n\n" +
                        if (hasNext) "Ready for the next one?" else "You've completed all puzzles for this technique!",
            )
            .setPositiveButton(if (hasNext) "Next puzzle" else "Done") { _, _ ->
                if (hasNext) loadTrainingPuzzle(trainingIndex + 1)
                else parentFragmentManager.popBackStack()
            }
            .setNegativeButton("Back to techniques") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    private fun showNoSamplesDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("No puzzles available")
            .setMessage("This technique has no SET-type training puzzles yet.\nTry another technique.")
            .setPositiveButton("Back") { _, _ -> parentFragmentManager.popBackStack() }
            .setCancelable(false)
            .show()
    }
}
