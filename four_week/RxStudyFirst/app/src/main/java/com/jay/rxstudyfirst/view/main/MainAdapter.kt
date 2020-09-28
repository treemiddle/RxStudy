package com.jay.rxstudyfirst.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ItemMovieBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainAdapter(
    private val click: (Movie) -> Unit
) : RecyclerView.Adapter<MainAdapter.MovieViewHolder>() {

    private val compositeDisposable = CompositeDisposable()
    private val hasLikeSubject = PublishSubject.create<Unit>()
    private val items = mutableListOf<Movie>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemMovieBinding.inflate(layoutInflater, parent, false)

        return MovieViewHolder(binding).also {
            binding.root.setOnClickListener { position ->
                hasLikeSubject.onNext(Unit)
                click(items[it.adapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        compositeDisposable.clear()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.movie = movie
            binding.executePendingBindings()
        }
    }

    fun onClickHasLiked(movie: Movie) {
        hasLikeSubject.map { System.currentTimeMillis() }
            .buffer(2, 1)
            .map { val (first, second) = it; first to second }
            .filter { (first, second) -> second - first < 1_000 }
            .subscribe { movie.hasLiked = !movie.hasLiked }
            .let(compositeDisposable::add)
    }

    fun setMovieItem(movies: List<Movie>) {
        clear()
        this.items.addAll(movies)
        notifyDataSetChanged()
    }

    fun clear() {
        this.items.clear()
        notifyDataSetChanged()
    }
}