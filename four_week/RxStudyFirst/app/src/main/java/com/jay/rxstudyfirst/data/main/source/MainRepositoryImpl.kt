package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieResponse
import com.jay.rxstudyfirst.data.main.source.local.MainLocalDataSource
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSource
import io.reactivex.Single

class MainRepositoryImpl(
    private val remoteDataSource: MainRemoteDataSource,
    private val localDataSource: MainLocalDataSource
) : MainRepository {

    override fun getMovie(query: String): Single<List<Movie>> {
        return remoteDataSource.getMovie(query)
            .map { it.data.movies }
            .flatMap { movies ->
                localDataSource.insertMovies(movies)
                    .andThen(Single.just(movies))
            }
    }

    override fun getMoreMovies(query: String, page: Int): Single<MovieResponse> {
        return remoteDataSource.getMoreMovies(query, page)
    }
}