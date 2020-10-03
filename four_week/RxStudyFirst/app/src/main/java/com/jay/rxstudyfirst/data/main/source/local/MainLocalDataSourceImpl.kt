package com.jay.rxstudyfirst.data.main.source.local

import com.jay.rxstudyfirst.data.MovieLikeEntity
import com.jay.rxstudyfirst.data.database.MovieLikeDao
import io.reactivex.Completable
import io.reactivex.Single

class MainLocalDataSourceImpl(
    private val dao: MovieLikeDao
) : MainLocalDataSource {

    override fun saveMovie(movieLike: MovieLikeEntity): Completable {
        return dao.saveMovieLike(movieLike)
    }

    override fun getMovieLike(id: Long): Single<MovieLikeEntity> {
        return dao.queryMovie(id)
    }
}