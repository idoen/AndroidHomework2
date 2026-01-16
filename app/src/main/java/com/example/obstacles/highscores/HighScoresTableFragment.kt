package com.example.obstacles.highscores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.obstacles.R

class HighScoresTableFragment : Fragment() {

    private val viewModel: HighScoresViewModel by activityViewModels {
        HighScoresViewModelFactory(HighScoresServiceLocator.repository(requireContext()))
    }

    private lateinit var adapter: HighScoresAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_high_scores_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.highScoresList)
        adapter = HighScoresAdapter { entry ->
            viewModel.select(entry)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.topScores.observe(viewLifecycleOwner) { scores ->
            adapter.submitList(scores)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}
