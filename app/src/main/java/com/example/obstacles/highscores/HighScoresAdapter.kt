package com.example.obstacles.highscores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.obstacles.R

class HighScoresAdapter(
    private val onClick: (HighScoreEntry) -> Unit
) : RecyclerView.Adapter<HighScoresAdapter.ScoreViewHolder>() {

    private var scores: List<HighScoreEntry> = emptyList()

    fun submitList(items: List<HighScoreEntry>) {
        scores = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_high_score, parent, false)
        return ScoreViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        holder.bind(scores[position], position + 1)
    }

    override fun getItemCount(): Int = scores.size

    class ScoreViewHolder(
        itemView: View,
        private val onClick: (HighScoreEntry) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val rankView: TextView = itemView.findViewById(R.id.scoreRank)
        private val valueView: TextView = itemView.findViewById(R.id.scoreValue)
        private val distanceView: TextView = itemView.findViewById(R.id.scoreDistance)
        private val locationView: TextView = itemView.findViewById(R.id.scoreLocation)

        fun bind(entry: HighScoreEntry, rank: Int) {
            rankView.text = itemView.context.getString(R.string.score_rank_format, rank)
            valueView.text = itemView.context.getString(
                R.string.high_score_coins_format,
                entry.coins
            )
            distanceView.text = itemView.context.getString(
                R.string.high_score_distance_format,
                entry.distance
            )
            locationView.text = entry.location.label
            itemView.setOnClickListener { onClick(entry) }
        }
    }
}
