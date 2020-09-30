package com.jay.rxstudyfirst.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ItemMovieBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

typealias recyclerviewItemClick = ((Movie, Int) -> Unit)

class MainAdapter(
    private val onItemClick: recyclerviewItemClick? = null
) : ListAdapter<Movie, MainAdapter.MovieHolder>(object : DiffUtil.ItemCallback<Movie>() {

    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.hasLiked == newItem.hasLiked
    }

}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        return MovieHolder.from(parent).also { holder ->
            if (onItemClick == null) {
                return@also
            } else {
                holder.itemView.setOnClickListener { _ ->
                    val currentItem = currentList.getOrNull(holder.adapterPosition)
                        ?: return@setOnClickListener

                    //currentItem.hasLiked = !currentItem.hasLiked
                    onItemClick.invoke(currentItem, holder.adapterPosition)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MovieHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.movie = movie
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup) : MovieHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemMovieBinding.inflate(layoutInflater, parent, false)

                return MovieHolder(binding)
            }
        }
    }
}