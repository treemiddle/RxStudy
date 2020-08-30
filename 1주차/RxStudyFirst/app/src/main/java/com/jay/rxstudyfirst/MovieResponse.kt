package com.jay.rxstudyfirst

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MovieResponse(

//    "status":"ok",
//"status_message":"Query was successful",
//"data"
    @SerializedName("movies")
    @Expose
    val movie: Movie
)