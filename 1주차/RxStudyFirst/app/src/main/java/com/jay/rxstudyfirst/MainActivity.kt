package com.jay.rxstudyfirst

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

/***
 * 1주차다시 올릴때 1주차 꺼는 firstweek or 1주차 꺼 찾아서 다시 올려야함
 */
class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MainAdapter
    private lateinit var progressbar: ProgressBar
    private lateinit var query: EditText
    private lateinit var button: Button

    private val querySubject = BehaviorSubject.create<String>()
    private val buttonClick = PublishSubject.create<Unit>()

    private val api by lazy {
        ApiService.api
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initAdapter()
        initTextWatcher()
        rxBind()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recycler_view)
        query = findViewById(R.id.et_query)
        progressbar = findViewById(R.id.progress_bar)
        button = findViewById(R.id.btn_result)

        button.setOnClickListener { buttonClick.onNext(Unit) }
    }

    private fun initTextWatcher() {
        query.addTextChangedListener { querySubject.onNext(it.toString()) }
    }

    private fun rxBind() {
        querySubject.debounce(1_000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {}
            .let(compositeDisposable::add)

        buttonClick.observeOn(AndroidSchedulers.mainThread())
            .subscribe { searchMovie(querySubject.value!!) }
            .let(compositeDisposable::add)
    }

    private fun searchMovie(query: String)  {
        val load = loadMore(query)
        load.subscribeOn(Schedulers.io())
            .doOnSubscribe { isLoading(true) }
            .doAfterTerminate { isLoading(false) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                adapter.setMovieItem(it)
            }, {
                toastMessage(it.message!!)
            }).let(compositeDisposable::add)
    }

    private fun getQuery(query: String, page: Int): Single<MovieResponse> {
        return api.getMovies(query, page).rxSingle()
    }

    private fun loadMore(query: String): Single<List<Movie>> {
        return Observable.range(1, 10)
            .concatMapEager { page ->
                getQuery(query, page)
                    .flatMapObservable { response ->
                        Observable.fromIterable(response.data.movies)
                    }
            }
            .toList()
    }

    private fun initAdapter() {
        adapter = MainAdapter { movie ->
            toastMessage("$movie")

        }
        recyclerView.adapter = adapter
    }

    private fun isLoading(loading: Boolean) {
        progressbar.visibility = if (loading) {
            View.VISIBLE
        } else {
            View.INVISIBLE
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

