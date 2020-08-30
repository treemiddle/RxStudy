package com.jay.rxstudyfirst

import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {

    @GET("list_movies.json")
    fun getMovies(): Call<MovieResponse>
}