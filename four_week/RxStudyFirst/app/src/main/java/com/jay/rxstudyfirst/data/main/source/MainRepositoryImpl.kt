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
    private val TAG = javaClass.simpleName

    override fun getMovie(query: String): Flowable<List<Movie>> {
        return localDataSource.getMovies(query)
            .flatMapPublisher { cacheMovies ->
                if (cacheMovies.isEmpty()) {
                    getRemoteMovies(query)
                        .toFlowable()
                } else {
                    Flowable.just(cacheMovies)
                }
            }
    }

    override fun getMoreMovies(query: String, page: Int): Single<List<Movie>> {
        return remoteDataSource.getMovie(query, page)
            .map { it.data.movies }
            .flatMap { movies ->
                if (movies.isEmpty()) {
                    Single.error(IllegalStateException("Anymore!"))
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
            .map { it.data.movies }
            .flatMap { movies ->
                if (movies.isEmpty()) {
                    Single.error(IllegalStateException("UnKnown Error"))
                } else {
                    localDataSource.insertMovies(movies)
                        .andThen(Single.just(movies))
                }
            }
    }

    private fun getRemotePagingMovies(query: String, page: Int): Single<List<Movie>> {
        return remoteDataSource.getMovie(query, page)
            .map { it.data.movies }
            .flatMap { pagingMovies ->
                if (pagingMovies.isEmpty()) {
                    Single.error(IllegalStateException("AnyMore!"))
                } else {
                    localDataSource.insertMovies(pagingMovies)
                        .andThen(Single.just(pagingMovies))
                }
            }
    }
}