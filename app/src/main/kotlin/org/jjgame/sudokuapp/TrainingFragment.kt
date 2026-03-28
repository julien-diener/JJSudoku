package org.jjgame.sudokuapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TrainingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_training, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnTrainingHome).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val recycler = view.findViewById<RecyclerView>(R.id.techniqueList)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        // Load in background to avoid blocking the main thread
        val ctx = requireContext().applicationContext
        Thread {
            val techniques = TrainingRepository.listTechniques(ctx)
            view.post {
                if (isAdded) {
                    recycler.adapter = TechniqueAdapter(techniques) { technique ->
                        startTraining(technique)
                    }
                }
            }
        }.start()
    }

    private fun startTraining(technique: TrainingTechnique) {
        val fragment = GameFragment.newTrainingInstance(
            assetPath = technique.assetPath,
            techniqueName = technique.name,
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}

// ── Adapter ─────────────────────────────────────────────────────────────────

private class TechniqueAdapter(
    private val items: List<TrainingTechnique>,
    private val onSelect: (TrainingTechnique) -> Unit,
) : RecyclerView.Adapter<TechniqueAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        val textName: TextView = itemView.findViewById(R.id.textTechniqueName)
        val textCount: TextView = itemView.findViewById(R.id.textSampleCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_technique, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val prev = items.getOrNull(position - 1)

        // Show category header only when it changes
        if (prev == null || prev.category != item.category) {
            holder.textCategory.visibility = View.VISIBLE
            holder.textCategory.text = item.category
        } else {
            holder.textCategory.visibility = View.GONE
        }

        holder.textName.text = item.name
        holder.textCount.text = "${item.sampleCount} puzzles"
        holder.itemView.setOnClickListener { onSelect(item) }
    }
}

