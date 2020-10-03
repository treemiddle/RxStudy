package com.jay.rxstudyfirst.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieLikeEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface MovieLikeDao {

    @Insert(onConflict = REPLACE)
    fun saveMovieLike(movieLike: MovieLikeEntity): Completable

    @Query("SELECT * FROM movie_like WHERE id = :id")
    fun queryMovie(id: Long): Single<MovieLikeEntity>
}