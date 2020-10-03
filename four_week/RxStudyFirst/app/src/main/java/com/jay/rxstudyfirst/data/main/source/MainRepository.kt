package com.jay.rxstudyfirst.data.main.source

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieLikeEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface MainRepository {

    fun saveMovieLike(movieLike: MovieLikeEntity): Completable

    fun getMovies(query: String, page: Int = 1): Flowable<List<Movie>>


    //    fun getMovie(query: String): Flowable<List<Movie>>
//
//    fun getMoreMovies(query: String, page: Int): Single<List<Movie>>
//
//    fun movieLike(movie: Movie): Completable
//
//    fun deleteAll(): Completable
}