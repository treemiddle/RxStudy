package com.jay.rxstudyfirst.data.main.source.remote

import com.jay.rxstudyfirst.api.ApiInterface
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieResponse
import com.jay.rxstudyfirst.utils.rxSingle
import io.reactivex.Single

class MainRemoteDataSourceImpl(
    private val remote: ApiInterface
) : MainRemoteDataSource {

    override fun getMovie(query: String): Single<List<Movie>> {
        return remote.getMovie(query).map { it.data.movies }
    }

    override fun getMoreMovies(query: String, page: Int): Single<List<Movie>> {
        return remote.getMoreMovies(query, page).map { it.data.movies }
    }
}