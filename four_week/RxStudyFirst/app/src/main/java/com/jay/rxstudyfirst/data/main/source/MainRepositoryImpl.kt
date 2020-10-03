package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieLikeEntity
import com.jay.rxstudyfirst.data.main.source.local.MainLocalDataSource
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

class MainRepositoryImpl(
    private val remoteDataSource: MainRemoteDataSource,
    private val localDataSource: MainLocalDataSource
) : MainRepository {
    private val TAG = javaClass.simpleName

    override fun saveMovieLike(movieLike: MovieLikeEntity): Completable {
        return localDataSource.saveMovie(movieLike)
    }

    override fun getMovies(query: String, page: Int): Flowable<List<Movie>> {
        return remoteDataSource.getMovie(query, page)
            .flatMapPublisher { remoteMovies ->
                if (remoteMovies.data.movies.isNotEmpty()) {
                    remoteMovie(query, page).toFlowable()
                } else {
                    Flowable.error(IllegalStateException("No Result"))
                }
            }
    }

    private fun remoteMovie(query: String, page: Int): Single<List<Movie>> {
        return remoteDataSource.getMovie(query, page)
            .flatMap { remoteMovie ->
                Observable.fromIterable(remoteMovie.data.movies)
                    .concatMapDelayError { movie ->
                        localDataSource.getMovieLike(movie.id).toObservable()
                            .map { entity -> if (entity.hasLiked) movie.hasLiked = entity.hasLiked }
                            .onErrorReturn {
                                movie.hasLiked = false
                            }
                            .map { movie }
                    }
                    .toList()
            }
    }


    /*.concatMapEager { movie ->
        localDataSource.getMovieLike(movie.id).toObservable()
            .map { entity -> if (entity.hasLiked) movie.hasLiked = entity.hasLiked }
            .onErrorReturn {
                movie.hasLiked = false
            }
            .map { movie }
    }*/


    //    override fun getMovie(query: String): Flowable<List<Movie>> {
//        return localDataSource.getMovies(query)
//            .flatMapPublisher { cacheMovies ->
//                if (cacheMovies.isNotEmpty()) {
//                    Flowable.just(cacheMovies)
//                } else {
//                    getRemoteMovies(query)
//                        .toFlowable()
//                }
//            }
//    }
//
//    override fun getMoreMovies(query: String, page: Int): Single<List<Movie>> {
//        return remoteDataSource.getMovie(query, page)
//            .flatMap { movies ->
//                if (movies.data.movies.isNullOrEmpty()) {
//                    Single.error(IllegalStateException("Last Page!"))
//                } else {
//                    getRemotePagingMovies(query, page)
//                }
//            }
//    }
//
//    override fun movieLike(movie: Movie): Completable {
//        return localDataSource.movieLike(movie)
//    }
//
//    override fun deleteAll(): Completable {
//        return localDataSource.deleteAll()
//    }
//
//    private fun getRemoteMovies(query: String): Single<List<Movie>> {
//        return remoteDataSource.getMovie(query)
//            .flatMap { movies ->
//                if (movies.data.movies.isNullOrEmpty()) {
//                    Single.error(IllegalStateException("No Result"))
//                } else {
//                    localDataSource.insertMovies(movies.data.movies)
//                        .andThen(Single.just(movies.data.movies))
//                }
//            }
//    }
//
//    private fun getRemotePagingMovies(query: String, page: Int): Single<List<Movie>> {
//        return remoteDataSource.getMovie(query, page)
//            .flatMap { pagingMovies ->
//                if (pagingMovies.data.movies.isNullOrEmpty()) {
//                    Single.error(IllegalStateException("No Paging Result!"))
//                } else {
//                    localDataSource.insertMovies(pagingMovies.data.movies)
//                        .andThen(Single.just(pagingMovies.data.movies))
//                }
//            }
//    }
}