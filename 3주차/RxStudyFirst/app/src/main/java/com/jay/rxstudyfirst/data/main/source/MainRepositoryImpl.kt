package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.MovieResponse
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSource
import io.reactivex.Single

class MainRepositoryImpl(
    private val mainRemoteDataSource: MainRemoteDataSource
) : MainRepository {

    override fun getMovie(query: String): Single<MovieResponse> {
        return mainRemoteDataSource.getMovie(query)
    }
}