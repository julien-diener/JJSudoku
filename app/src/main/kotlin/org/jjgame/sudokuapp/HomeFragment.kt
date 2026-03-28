package org.jjgame.sudokuapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.jjgame.sudoku.Difficulty

class HomeFragment : Fragment() {

    private lateinit var gameViewModel: SudokuGameViewModel
    private lateinit var btnContinueGame: Button
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button
    private lateinit var btnUnfair: Button
    private lateinit var btnExtreme: Button
    private lateinit var btnTraining: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gameViewModel = ViewModelProvider(requireActivity())[SudokuGameViewModel::class.java]

        btnContinueGame = view.findViewById(R.id.btnContinueGame)
        btnEasy    = view.findViewById(R.id.btnEasy)
        btnMedium  = view.findViewById(R.id.btnMedium)
        btnHard    = view.findViewById(R.id.btnHard)
        btnUnfair  = view.findViewById(R.id.btnUnfair)
        btnExtreme = view.findViewById(R.id.btnExtreme)
        btnTraining = view.findViewById(R.id.btnTraining)

        // Show "Continue Game" button only if a saved game exists
        if (gameViewModel.hasSavedGame()) {
            btnContinueGame.visibility = View.VISIBLE
            btnContinueGame.setOnClickListener {
                gameViewModel.loadSavedGame()
                navigateToGame()
            }
        }

        btnEasy.setOnClickListener    { startNewGame(Difficulty.EASY)    }
        btnMedium.setOnClickListener  { startNewGame(Difficulty.MEDIUM)  }
        btnHard.setOnClickListener    { startNewGame(Difficulty.HARD)    }
        btnUnfair.setOnClickListener  { startNewGame(Difficulty.UNFAIR)  }
        btnExtreme.setOnClickListener { startNewGame(Difficulty.EXTREME) }
        btnTraining.setOnClickListener { navigateToTraining() }
    }

    private fun startNewGame(difficulty: Difficulty) {
        val loadingDialog = AlertDialog.Builder(requireContext())
            .setTitle("Please wait")
            .setMessage("Generating puzzle...")
            .setCancelable(false)
            .create()

        loadingDialog.show()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                gameViewModel.startGameAsync(difficulty)
                if (!isAdded) return@launch
                navigateToGame()
            } finally {
                loadingDialog.dismiss()
            }
        }
    }

    private fun navigateToGame() {
        val fragment = GameFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToTraining() {
        val fragment = TrainingFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
