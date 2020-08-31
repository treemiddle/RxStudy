package com.jay.rxstudyfirst

import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {

    //    @GET("list_movies.json")
//    fun getMovies(): Single<MovieResponse>
//    @GET("list_movies.json")
//    fun getMovies(): Observable<Call<MovieResponse>>
    @GET("list_movies.json")
    fun getMovies(): Observable<MovieResponse>
}