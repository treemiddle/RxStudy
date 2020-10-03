package com.jay.rxstudyfirst.data.main.source.local

import com.jay.rxstudyfirst.data.MovieLikeEntity
import io.reactivex.Completable
import io.reactivex.Single

interface MainLocalDataSource {

    fun saveMovie(movieLike: MovieLikeEntity): Completable

    fun getMovieLike(id: Long): Single<MovieLikeEntity>

}