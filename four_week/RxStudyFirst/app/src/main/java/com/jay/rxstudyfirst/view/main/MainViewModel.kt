package com.jay.rxstudyfirst.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.main.source.MainRepository
import com.jay.rxstudyfirst.utils.SingleLiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.merge
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainViewModel(private val mainRepository: MainRepository) {

    private val compositeDisposable = CompositeDisposable()
    private var disposable: Disposable? = null
    private val querySubject = BehaviorSubject.create<String>()
    private val onSearchClick = PublishSubject.create<Unit>()

    private val _isLoading = MutableLiveData(false)
    private val _movieList = SingleLiveEvent<List<Movie>>()
    private val _fail = SingleLiveEvent<String>()

    val movieList: LiveData<List<Movie>> get() = _movieList
    val isLoading: LiveData<Boolean> get() = _isLoading
    val fail: LiveData<String> get() = _fail

    init {
        rxBind()
    }

    private fun getMovie(query: String) {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }

        disposable = mainRepository.getMovie(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({ response ->
                println("success: $response")
//                with(response.data.movies) {
//                    when {
//                        this.isNullOrEmpty() -> _fail.value = "검색 결과가 없습니다"
//                        else -> _movieList.value = response.data.movies
//                    }
//                }
            }, { t ->
                _fail.value = t.message
            }).addTo(compositeDisposable)
    }

    fun getMoreMovies(page: Int) {
        println("===== getMoreMovies: $page =====")
        mainRepository.getMoreMovies(querySubject.value!!, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({ response ->
                with(response.data.movies) {
                    when {
                        this.isNullOrEmpty() -> _fail.value = "??????"
                        else -> {
                            val pagingList = _movieList.value as ArrayList<Movie>
                            pagingList.addAll(response.data.movies)
                            _movieList.value = pagingList
                        }
                    }
                }
            }, { t ->
                _fail.value = t.message
            }).addTo(compositeDisposable)
    }

    fun onSearchClick() {
        onSearchClick.onNext(Unit)
    }

    fun queryOnNext(query: String?) {
        query?.let { querySubject.onNext(query) }
    }

    private fun showNullQuery() {
        _fail.value = "영화를 검색해 주세요"
    }

    private fun searchButtonClick(): Observable<String> {
        return onSearchClick.throttleFirst(2_000, TimeUnit.MILLISECONDS)
            .map { querySubject.value ?: "" }
            .distinctUntilChanged()
    }

    private fun searchMovie(): Observable<String> {
        return querySubject.debounce(1_000, TimeUnit.MILLISECONDS)
            .filter { it.length >= 2 }
    }

    private fun rxBind() {
        val movieQuery = searchButtonClick()
        val searchClick = searchMovie()

        listOf(movieQuery, searchClick)
            .merge()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query ->
                if (query.isNullOrEmpty()) showNullQuery()
                else getMovie(query)
            }
            .addTo(compositeDisposable)
    }

    private fun showLoading() {
        _isLoading.value = true
    }

    private fun hideLoading() {
        _isLoading.value = false
    }

}