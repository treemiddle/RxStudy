package com.jay.rxstudyfirst

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

//    @GET("list_movies.json")
//    fun getMovies(): Call<MovieResponse>

    @GET("list_movies.json")
    fun getMovies(
        @Query("query_term") query: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 30
    ): Call<MovieResponse>
}