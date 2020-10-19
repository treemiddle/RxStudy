package com.jay.rxstudyfirst.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MovieResponse(
    @SerializedName("data")
    @Expose
    val data: Movies
)

data class Movies(
    @SerializedName("movies")
    @Expose
    val movies: List<Movie>?
)