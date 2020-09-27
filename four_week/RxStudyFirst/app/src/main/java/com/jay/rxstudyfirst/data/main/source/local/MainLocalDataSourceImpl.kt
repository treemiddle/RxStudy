package com.jay.rxstudyfirst.data.main.source.local

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.database.MovieDao
import io.reactivex.Completable

class MainLocalDataSourceImpl(
    private val dao: MovieDao
) : MainLocalDataSource {

//    override fun test(movies: List<Movie>): Completable {
//        return dao.test(movies)
//    }
}