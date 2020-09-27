package com.jay.rxstudyfirst.data.main.source.remote

import com.jay.rxstudyfirst.api.ApiInterface
import com.jay.rxstudyfirst.data.MovieResponse
import com.jay.rxstudyfirst.utils.rxSingle
import io.reactivex.Single

class MainRemoteDataSourceImpl(
    private val remote: ApiInterface
) : MainRemoteDataSource {

    override fun getMovie(query: String): Single<MovieResponse> {
        return remote.getMovie(query).rxSingle()
    }

    override fun getMoreMovies(query: String, page: Int): Single<MovieResponse> {
        return remote.getMoreMovies(query, page).rxSingle()
    }
}