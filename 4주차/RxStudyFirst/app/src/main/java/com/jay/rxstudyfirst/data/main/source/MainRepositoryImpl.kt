package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieResponse
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSource
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.intellij.lang.annotations.Flow

class MainRepositoryImpl(
    private val dataSource: MainRemoteDataSource
) : MainRepository {

    override fun getMovie(query: String, page: Int): Single<MovieResponse> {
        return dataSource.getMovie(query, page)
    }

    override fun getMore(query: String): Single<List<Movie>> {
        return loadMore(query)
    }

    private fun loadMore(query: String): Single<List<Movie>> {
        return Observable.range(1,10)
            .concatMap { page ->
                getMovie(query, page)
                    .flatMapObservable { response ->
                        Observable.fromIterable(response.data.movies)
                    }
            }
            .toList()
    }
}