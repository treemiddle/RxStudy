package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieResponse
import io.reactivex.Flowable
import io.reactivex.Single

interface MainRepository {

    fun getMovie(query: String, page: Int): Single<MovieResponse>

    fun getMore(query: String): Single<List<Movie>>

}