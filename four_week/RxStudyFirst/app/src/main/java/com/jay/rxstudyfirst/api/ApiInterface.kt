package com.jay.rxstudyfirst.api

import com.jay.rxstudyfirst.data.MovieResponse
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("list_movies.json")
    fun getMovies(): Call<MovieResponse>

    @GET("list_movies.json")
    fun getMovie(
        @Query("query_term") query: String
    ): Single<MovieResponse>

    @GET("list_movies.json")
    fun getMoreMovies(
        @Query("query_term") query: String,
        @Query("page") page: Int
    ): Single<MovieResponse>

}