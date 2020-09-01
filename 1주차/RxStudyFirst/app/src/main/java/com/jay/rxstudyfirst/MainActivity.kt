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
import io.reactivex.Single
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
    private lateinit var btnSingle: Button
    private lateinit var btnMaybe: Button
    private lateinit var btnCompletable: Button
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
        btnSingle = findViewById(R.id.btn_single)
        btnMaybe = findViewById(R.id.btn_maybe)
        btnCompletable = findViewById(R.id.btn_completable)
        progressbar = findViewById(R.id.progress_bar)
    }

    private fun initAdapter() {
        adapter = MainAdapter { movie ->
            Log.d(TAG, "initAdapter: $movie")
        }

        recyclerView.adapter = adapter
    }

    private fun initClickListener() {
//        btnSingle.setOnClickListener {
//            api.getMovies().rxSingle()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { isLoading(true) }
//                .doAfterTerminate { isLoading(false) }
//                .subscribe({
//                    Log.d(TAG, "ok: $it")
//                }, {
//                    Log.d(TAG, "initClickListener: ${it.message}")
//                }).addTo(compositeDisposable)
//        }
//
//        btnMaybe.setOnClickListener {
//
//        }
//
//        btnCompletable.setOnClickListener {
//
//        }
    }

    private fun isLoading(loading: Boolean) {
        progressbar.visibility = if (loading) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun <T> Call<T>.rxSingle(): Single<T> {
        return Single.create { emitter ->
            this.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    emitter.onError(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        emitter.onSuccess(body)
                    } else {
                        emitter.onError(Throwable("server error"))
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
