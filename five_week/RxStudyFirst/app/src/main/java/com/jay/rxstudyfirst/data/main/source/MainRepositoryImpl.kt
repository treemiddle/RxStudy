package com.jay.rxstudyfirst.data.main.source

import android.util.Log
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieLikeEntity
import com.jay.rxstudyfirst.data.main.source.local.MainLocalDataSource
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSource
import com.jay.rxstudyfirst.utils.NetworkManager
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.HttpException
import java.lang.RuntimeException
import java.net.HttpRetryException

class MainRepositoryImpl(
    private val remoteDataSource: MainRemoteDataSource,
    private val localDataSource: MainLocalDataSource,
    private val networkManager: NetworkManager
) : MainRepository {
    private val TAG = javaClass.simpleName
    override fun saveMovieLike(movieLike: MovieLikeEntity): Completable {
        return localDataSource.saveMovie(movieLike)
    }

    override fun getMovies(query: String, page: Int): Single<List<Movie>> {
        return if (networkManager.networkState()) {
            remoteDataSource.getMovie(query, page)
                .flatMap { remoteMovie(query, page) }
        } else {
            Single.error(IllegalStateException("Network Error"))
        }
    }

    override fun getNetworkState(): Single<Boolean> {
        return networkState()
    }

    override fun test(): Boolean {
        return networkManager.networkState()
    }

    private fun remoteMovie(query: String, page: Int): Single<List<Movie>> {
        return remoteDataSource.getMovie(query, page)
            .flatMap { remoteMovie ->
                Observable.fromIterable(remoteMovie.data.movies)
                    .concatMapEagerDelayError({ movie ->
                        Observable.just(movie.id)
                            .flatMap { id ->
                                localDataSource.getMovieLike(id)
                                    .toObservable()
                                    .map(MovieLikeEntity::hasLiked)
                                    .onErrorReturnItem(false)
                                    .map { hasLiked -> movie.copy(hasLiked = hasLiked) }
                            }
                    }, true)
                    .toList()
            }
    }

    private fun networkState(): Single<Boolean> {
        return Single.create { emitter ->
            val result = networkManager.networkState()

            if (result) {
                Log.d(TAG, "networkState: if")
                emitter.onSuccess(result)
            } else {
                Log.d(TAG, "networkState: else")
                emitter.onError(Throwable("error...."))
            }
        }
    }

}