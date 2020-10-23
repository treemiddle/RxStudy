package com.jay.rxstudyfirst.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_like")
data class MovieLikeEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val hasLiked: Boolean
)