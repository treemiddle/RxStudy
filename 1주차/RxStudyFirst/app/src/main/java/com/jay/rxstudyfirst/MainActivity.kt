package com.jay.rxstudyfirst

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.utils.widget.MockView
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnApiCall: Button
    private lateinit var adapter: MainAdapter
    private lateinit var call: Call<MovieResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initAdapter()
        initClickListener()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recycler_view)
        btnApiCall = findViewById(R.id.btn_call)
    }

    private fun initAdapter() {
        adapter = MainAdapter { movie ->
            Log.d(TAG, "initAdapter: $movie")
        }

        recyclerView.adapter = adapter
    }

    private fun initClickListener() {
        btnApiCall.setOnClickListener {
            val sss = ApiService.api
            call = sss.getMovies()

            call.enqueue(object : Callback<MovieResponse> {
                override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                    Log.d(TAG, "onFailure: ${t.message}")
                }

                override fun onResponse(
                    call: Call<MovieResponse>,
                    response: Response<MovieResponse>
                ) {
                    Log.d(TAG, "onResponse: $response")
                    Log.d(TAG, "onResponse: ${response.raw()}")
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        Log.d(TAG, "onResponse: $body")
                        Log.d(TAG, "onResponse: ${body.movie}")
                    } else {
                        Log.d(TAG, "onResponse: ${response.message()}")
                    }
                }
            })
        }
    }
}