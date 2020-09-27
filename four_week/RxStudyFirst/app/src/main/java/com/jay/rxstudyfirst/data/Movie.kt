package com.jay.rxstudyfirst.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movie")
data class Movie(
    @SerializedName("id")
    @Expose
    val id: Int,

    @SerializedName("url")
    @Expose
    val url: String,

    @PrimaryKey(autoGenerate = false)
    @SerializedName("title")
    @Expose
    val title: String,

    @SerializedName("year")
    @Expose
    val year: Int,

    @SerializedName("rating")
    @Expose
    val rating: Float?,

    @SerializedName("genres")
    @Expose
    val genres: List<String>,

    @SerializedName("summary")
    @Expose
    val summary: String,

    @SerializedName("medium_cover_image")
    @Expose
    val poster: String,

    @Expose
    var hasLiked: Boolean = false
)