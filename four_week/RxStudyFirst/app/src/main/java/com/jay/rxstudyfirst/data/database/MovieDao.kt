package com.jay.rxstudyfirst.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.jay.rxstudyfirst.data.Movie
import io.reactivex.Completable
import io.reactivex.Single


@Dao
interface MovieDao {

    @Insert(onConflict = REPLACE)
    fun insertMovies(movies: List<Movie>): Completable

    @Query("SELECT * FROm movie WHERE title LIKE '%' || :title || '%' ORDER BY id DESC")
    fun getMovies(title: String): Single<List<Movie>>

    @Update
    fun movieLike(movie: Movie): Completable

    @Query("DELETE FROM movie")
    fun deleteAll(): Completable
}