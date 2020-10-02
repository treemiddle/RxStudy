package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.main.source.local.MainLocalDataSource
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class MainRepositoryImpl(
    private val remoteDataSource: MainRemoteDataSource,
    private val localDataSource: MainLocalDataSource
) : MainRepository {

    override fun getMovie(query: String): Flowable<List<Movie>> {
        return localDataSource.getMovies(query)
            .flatMapPublisher { cacheMovies ->
                if (cacheMovies.isNotEmpty()) {
                    Flowable.just(cacheMovies)
                } else {
                    getRemoteMovies(query)
                        .toFlowable()
                }
            }
    }

    override fun getMoreMovies(query: String, page: Int): Single<List<Movie>> {
        return remoteDataSource.getMovie(query, page)
            .flatMap { movies ->
                if (movies.data.movies.isNullOrEmpty()) {
                    Single.error(IllegalStateException("Last Page!"))
                } else {
                    getRemotePagingMovies(query, page)
                }
            }
    }

    override fun movieLike(movie: Movie): Completable {
        return localDataSource.movieLike(movie)
    }

    override fun deleteAll(): Completable {
        return localDataSource.deleteAll()
    }

    private fun getRemoteMovies(query: String): Single<List<Movie>> {
        return remoteDataSource.getMovie(query)
            .flatMap { movies ->
                if (movies.data.movies.isNullOrEmpty()) {
                    Single.error(IllegalStateException("No Result"))
                } else {
                    localDataSource.insertMovies(movies.data.movies)
                        .andThen(Single.just(movies.data.movies))
                }
            }
    }

    private fun getRemotePagingMovies(query: String, page: Int): Single<List<Movie>> {
        return remoteDataSource.getMovie(query, page)
            .flatMap { pagingMovies ->
                if (pagingMovies.data.movies.isNullOrEmpty()) {
                    Single.error(IllegalStateException("No Paging Result!"))
                } else {
                    localDataSource.insertMovies(pagingMovies.data.movies)
                        .andThen(Single.just(pagingMovies.data.movies))
                }
            }
    }
}