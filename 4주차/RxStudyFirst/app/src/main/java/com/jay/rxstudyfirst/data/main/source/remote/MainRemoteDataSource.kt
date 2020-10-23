package com.jay.rxstudyfirst.data.main.source.remote

import com.jay.rxstudyfirst.data.MovieResponse
import io.reactivex.Single

interface MainRemoteDataSource {

    fun getMovie(query: String, page: Int): Single<MovieResponse>
}