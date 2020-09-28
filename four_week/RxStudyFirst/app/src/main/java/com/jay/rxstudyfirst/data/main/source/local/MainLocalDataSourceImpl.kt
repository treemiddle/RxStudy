package com.jay.rxstudyfirst.data.main.source.local

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.database.MovieDao
import io.reactivex.Completable

class MainLocalDataSourceImpl(
    private val dao: MovieDao
) : MainLocalDataSource {

    override fun insertMovies(movies: List<Movie>): Completable {
        println("local movies: ${movies.size}")
        return dao.insertMovies(movies)
    }
}