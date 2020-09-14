package com.jay.rxstudyfirst

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MainAdapter(
    private val click: (Movie) -> Unit
) : RecyclerView.Adapter<MainAdapter.MovieViewHolder>() {

    private val items = mutableListOf<Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_movie, parent, false)

        return MovieViewHolder(view).also {
            view.setOnClickListener { position ->
                click(items[it.adapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val movieName = itemView.findViewById<TextView>(R.id.tv_movie_name)
        private val movieYear = itemView.findViewById<TextView>(R.id.tv_movie_year)
        private val moviePoster = itemView.findViewById<ImageView>(R.id.iv_poster)
        private val movieGenres = itemView.findViewById<TextView>(R.id.tv_movie_genres)
        private val movieSummary = itemView.findViewById<TextView>(R.id.tv_movie_summary)
        private val movieScore = itemView.findViewById<RatingBar>(R.id.rating_bar)

        fun bind(movie: Movie) {
            with(movie) {
                Glide.with(itemView).load(poster)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(moviePoster)
                movieName.text = title
                movieYear.text = year.toString()
                movieGenres.text = genres.joinToString(" | ")
                movieSummary.text = summary
                movieScore.rating = rating ?: 0f
            }
        }
    }

    fun setMovieItem(movies: List<Movie>) {
        this.items.addAll(movies)
        notifyDataSetChanged()
    }

    fun clear() {
        this.items.clear()
        notifyDataSetChanged()
    }
}