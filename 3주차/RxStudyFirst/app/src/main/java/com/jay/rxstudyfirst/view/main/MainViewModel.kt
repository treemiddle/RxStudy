package com.jay.rxstudyfirst.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.main.source.MainRepository
import com.jay.rxstudyfirst.utils.SingleLiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class MainViewModel(private val mainRepository: MainRepository) {

    private val compositeDisposable = CompositeDisposable()
    private val querySubject = BehaviorSubject.create<String>()
    private val onSearchClick = BehaviorSubject.create<String>()

    private val _isLoading = MutableLiveData(false)
    private val _movieList = SingleLiveEvent<List<Movie>>()
    private val _fail = SingleLiveEvent<String>()

    val movieList: LiveData<List<Movie>> get() = _movieList
    val isLoading: LiveData<Boolean> get() = _isLoading
    val fail: LiveData<String> get() = _fail

    fun getMovie() {
        mainRepository.getMovie(querySubject.value!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({ response ->
                with(response.data.movies) {
                    when {
                        this.isNullOrEmpty() -> _fail.value = "검색 결과가 없습니다"
                        else -> _movieList.value = response.data.movies
                    }
                }
            }, { t ->
                _fail.value = t.message
            }).let(compositeDisposable::add)
    }

    fun onSearchClick() {
        when {
            querySubject.value.isNullOrEmpty() -> _fail.value = "영화를 입력하세요"
            else -> onSearchClick.onNext(querySubject.value!!)
        }
    }

    fun queryOnNext(query: String?) {
        query?.let { querySubject.onNext(query) }
    }

    fun searchButtonClick(): Observable<String> {
        return onSearchClick.throttleFirst(2_000, TimeUnit.MILLISECONDS)
            .filter { querySubject.value!! != it }
            .distinctUntilChanged()
    }

    fun searchMovie(): Observable<String> {
        return querySubject.debounce(1_000, TimeUnit.MILLISECONDS)
            .filter { it.length >= 2 }
    }

    private fun showLoading() {
        _isLoading.value = true
    }

    private fun hideLoading() {
        _isLoading.value = false
    }
}