package com.jay.rxstudyfirst

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.utils.widget.MockView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
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
        btnSingle.setOnClickListener {
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

        btnMaybe.setOnClickListener {
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
                })
        }

        btnCompletable.setOnClickListener {
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
                })
        }
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
                        emitter.onError(Throwable("error"))
                    }
                }
            })
        }
    }

    private fun <T> Call<T>.rxMaybe(): Maybe<T> {
        return Maybe.create { emitter ->
            this.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    emitter.onError(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    when (response.isSuccessful) {
                        true -> body?.let {
                            emitter.onSuccess(it)
                        } ?: emitter.onComplete()
                        false -> emitter.onError(Throwable("error"))
                    }
                }
            })
        }
    }

    private fun <T> Call<T>.rxCompletable(): Completable {
        return Completable.create { emitter ->
            this.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    emitter.onError(t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()

                    if (response.isSuccessful && body != null) {
                        emitter.onComplete()
                    } else {
                        emitter.onError(Throwable("error"))
                    }
                }
            })
        }
    }

    private fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
