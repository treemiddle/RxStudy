package com.jay.rxstudyfirst.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.REPLACE
import com.jay.rxstudyfirst.data.Movie
import io.reactivex.Completable


@Dao
interface MovieDao {

    @Insert(onConflict = REPLACE)
    fun test(movies: List<Movie>): Completable
}