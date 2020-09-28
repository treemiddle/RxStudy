package com.jay.rxstudyfirst.data.main.source.local

import com.jay.rxstudyfirst.data.Movie
import io.reactivex.Completable

interface MainLocalDataSource {

    fun insertMovies(movies: List<Movie>): Completable
}