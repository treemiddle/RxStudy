package com.jay.rxstudyfirst.data.main.source.local

import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.database.MovieDao
import io.reactivex.Completable
import io.reactivex.Single

class MainLocalDataSourceImpl(
    private val dao: MovieDao
) : MainLocalDataSource {

    override fun insertMovies(movies: List<Movie>): Completable {
        return dao.insertMovies(movies)
    }

    override fun getMovies(query: String): Single<List<Movie>> {
        return dao.getMovies(query)
    }

    override fun movieLike(movie: Movie): Completable {
        return dao.movieLike(movie)
    }

    override fun deleteAll(): Completable {
        return dao.deleteAll()
    }
}