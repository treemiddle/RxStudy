package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieResponse
import io.reactivex.Single

interface MainRepository {

    fun getMovie(query: String): Single<List<Movie>>

    fun getMoreMovies(query: String, page: Int): Single<MovieResponse>
}