package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.MovieResponse
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSource
import io.reactivex.Single

class MainRepositoryImpl(
    private val dataSource: MainRemoteDataSource
) : MainRepository {

    override fun getMovie(query: String): Single<MovieResponse> {
        return dataSource.getMovie(query)
    }

    override fun getMoreMovies(query: String, page: Int): Single<MovieResponse> {
        return dataSource.getMoreMovies(query, page)
    }
}