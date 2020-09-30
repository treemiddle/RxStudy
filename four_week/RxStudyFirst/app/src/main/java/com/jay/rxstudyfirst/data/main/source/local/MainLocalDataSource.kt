package com.jay.rxstudyfirst.data.main.source.local

import com.jay.rxstudyfirst.data.Movie
import io.reactivex.Completable
import io.reactivex.Single

interface MainLocalDataSource {

    fun insertMovies(movies: List<Movie>): Completable

    fun getMovies(query: String): Single<List<Movie>>

    fun movieLike(movie: Movie): Completable

    fun deleteAll(): Completable
}