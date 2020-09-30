package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface MainRepository {

    fun getMovie(query: String): Flowable<List<Movie>>

    fun getMoreMovies(query: String, page: Int): Single<List<Movie>>

    fun movieLike(movie: Movie): Completable

    fun deleteAll(): Completable
}