package com.jay.rxstudyfirst.view.main

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.jay.rxstudyfirst.*
import com.jay.rxstudyfirst.api.ApiService
import com.jay.rxstudyfirst.utils.rxCompletable
import com.jay.rxstudyfirst.utils.rxMaybe
import com.jay.rxstudyfirst.utils.rxSingle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class MainActivity : AppCompatActivity() {

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
            toastMessage("$movie")
        }

        recyclerView.adapter = adapter
    }

    private fun initClickListener() {
        btnSingle.setOnClickListener { callSingle() }
        btnMaybe.setOnClickListener { callMaybe() }
        btnCompletable.setOnClickListener { callCompletable() }
    }

    private fun isLoading(loading: Boolean) {
        progressbar.visibility = if (loading) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
    }

    private fun callSingle() {
        api.getMovies().rxSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { isLoading(true) }
            .doAfterTerminate { isLoading(false) }
            .subscribe({ response ->
                with(adapter) {
                    clear()
                    setMovieItem(response.data.movies)
                    toastMessage("single success")
                }
            }, { t ->
                when (t) {
                    is HttpException -> toastMessage(t.toString())
                    else -> toastMessage("${t.message}")
                }
            }).addTo(compositeDisposable)
    }

    private fun callMaybe() {
        api.getMovies().rxMaybe()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { isLoading(true) }
            .doAfterTerminate { isLoading(false) }
            .subscribe({ response ->
                with(adapter) {
                    clear()
                    setMovieItem(response.data.movies)
                    toastMessage("maybe success")
                }
            }, { t ->
                when (t) {
                    is HttpException -> toastMessage(t.toString())
                    else -> toastMessage("${t.message}")
                }
            }).addTo(compositeDisposable)
    }

    private fun callCompletable() {
        api.getMovies().rxCompletable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { isLoading(true) }
            .doAfterTerminate { isLoading(false) }
            .subscribe({
                toastMessage("completable success!")
            }, { t ->
                when (t) {
                    is HttpException -> toastMessage(t.toString())
                    else -> toastMessage("${t.message}")
                }
            }).addTo(compositeDisposable)
    }

    private fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}

