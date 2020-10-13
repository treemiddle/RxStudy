package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieLikeEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface MainRepository {

    fun saveMovieLike(movieLike: MovieLikeEntity): Completable

    fun getMovies(query: String, page: Int = 1): Single<List<Movie>>

    fun getNetworkState(): Single<Boolean>

    fun test(): Boolean
}