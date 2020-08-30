package com.jay.rxstudyfirst

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainAdapter : RecyclerView.Adapter<MainAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMovieName = itemView.findViewById<TextView>(R.id.tv_movie_name)
        private val tvMovieYear = itemView.findViewById<TextView>(R.id.tv_movie_year)
        private val tvMovieActor = itemView.findViewById<TextView>(R.id.tv_movie_actor)
        private val tvMovieSummary = itemView.findViewById<TextView>(R.id.tv_movie_summary)
        private val ratingBar = itemView.findViewById<TextView>(R.id.rating_bar)

        fun bind() {

        }
    }
}