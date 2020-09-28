package com.jay.rxstudyfirst.utils

import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.jay.rxstudyfirst.R

@BindingAdapter("setMoviePoster")
fun setMoviePoster(iv: ImageView, url: String?) {
    iv.loadImage(iv, url)
}

@BindingAdapter("setHasLiked")
fun setHasLiked(iv: ImageView, like: Boolean) {
    if (like) {
        iv.setBackgroundResource(R.drawable.ic_like)
    } else {
        iv.setBackgroundResource(R.drawable.ic_unlike)
    }
}

@BindingAdapter("setMovieYear")
fun setMovieYear(tv: TextView, year: Int) {
    tv.text = year.toString()
}

@BindingAdapter("setMovieGenres")
fun setMovieGenres(tv: TextView, genres: List<String>?) {
    if (genres == null) {
        tv.text = "no data"
    } else {
        tv.text = genres.joinToString(" | ")
    }
}

@BindingAdapter("setMovieRating")
fun setMovieRating(rb: RatingBar, rating: Float?) {
    rb.rating = rating ?: 0f
}