package com.jay.rxstudyfirst

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.utils.widget.MockView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName

    private val compositeDisposable = CompositeDisposable()
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnApiCall: Button
    private lateinit var adapter: MainAdapter
    private lateinit var progressbar: ProgressBar

    private val api by lazy {
        ApiService.api
    }

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
        progressbar = findViewById(R.id.progress_bar)
    }

    private fun initAdapter() {
        adapter = MainAdapter { movie ->
            Log.d(TAG, "initAdapter: $movie")
        }

        recyclerView.adapter = adapter
    }

    private fun initClickListener() {
        btnApiCall.setOnClickListener {
            asdf(api.getMovies())
            api.getMovies().test()
//            api.getMovies().subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { isLoading(true) }
//                .doAfterTerminate { isLoading(false) }
//                .subscribe({
//                    Log.d(TAG, "success: $it")
//                }, {
//                    Log.d(TAG, "fail: ${it.message}")
//                }).addTo(compositeDisposable)
        }
    }

    private fun isLoading(loading: Boolean) {
        progressbar.visibility = if (loading) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    fun <T> test(ooo: Call<T>) : Observable<T> {
        ooo.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                Log.d(TAG, "onResponse: $response")
            }
        })

    }

//    fun <T> test (call: Call<MovieResponse>, c: Observable<MovieResponse>): Observable<T> {
//        call.enqueue(object : Callback<MovieResponse> {
//            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
//                TODO("Not yet implemented")
//            }
//        })
//
//    }
    fun <T> asdf(ccc: Observable<Call<T>>): T {
            ccc.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    it.enqueue(object : Callback<MovieResponse> {
                        override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                            Log.d(TAG, "onFailure: ${t.message}")
                        }

                        override fun onResponse(
                            call: Call<MovieResponse>,
                            response: Response<MovieResponse>
                        ) {
                            val body = response.body()
                            if (response.isSuccessful && body != null) {
                                Log.d(TAG, "onResponse: $body")
                            } else {
                                Log.d(TAG, "onResponse: else")
                            }
                        }
                    })
                }, {
                    Log.d(TAG, "asdf: ${it.message}")
                }).addTo(compositeDisposable)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
