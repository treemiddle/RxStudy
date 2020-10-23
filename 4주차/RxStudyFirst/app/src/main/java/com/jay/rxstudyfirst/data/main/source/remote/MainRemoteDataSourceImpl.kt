package com.jay.rxstudyfirst.data.main.source.remote

import com.jay.rxstudyfirst.api.ApiInterface
import com.jay.rxstudyfirst.data.MovieResponse
import com.jay.rxstudyfirst.utils.rxSingle
import io.reactivex.Single

class MainRemoteDataSourceImpl(
    private val remote: ApiInterface
) : MainRemoteDataSource {

    override fun getMovie(query: String, page: Int): Single<MovieResponse> {
        return remote.getMovie(query, page).rxSingle()
    }
}