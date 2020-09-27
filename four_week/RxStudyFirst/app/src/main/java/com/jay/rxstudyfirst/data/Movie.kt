package com.jay.rxstudyfirst.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movie")
data class Movie @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id")
    @Expose
    val id: Int,

    @SerializedName("url")
    @Expose
    val url: String,

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
    @Ignore
    val genres: List<String>? = null,

    @SerializedName("summary")
    @Expose
    val summary: String,

    @SerializedName("medium_cover_image")
    @Expose
    val poster: String,

    var hasLiked: Boolean = false
)