package com.jay.rxstudyfirst.view.main

import android.accounts.NetworkErrorException
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieLikeEntity
import com.jay.rxstudyfirst.data.main.source.MainRepository
import com.jay.rxstudyfirst.utils.SingleLiveEvent
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException
import java.net.HttpRetryException
import java.nio.channels.FileLock
import java.util.concurrent.TimeUnit
import kotlin.math.log

class MainViewModel(private val mainRepository: MainRepository) {

    private val TAG = javaClass.simpleName
    private val compositeDisposable = CompositeDisposable()
    private var disposable: Disposable? = null
    private val querySubject = BehaviorSubject.create<String>()
    private val onSearchClick = PublishSubject.create<Unit>()
    private val movieLiked = BehaviorSubject.create<Movie>()
    private val moviePosition = BehaviorSubject.create<Int>()
    private val movieRefresh = PublishSubject.create<Unit>()
    private val networkRetry = PublishSubject.create<Unit>()
    private val onAvailable = BehaviorSubject.createDefault(0)

    private val _isLoading = MutableLiveData(false)
    private val _movieList = MutableLiveData<List<Movie>>()
    private val _fail = SingleLiveEvent<String>()
    private val _swipe = SingleLiveEvent<Unit>()
    private val _error = SingleLiveEvent<StateMessage>()

    val movieList: LiveData<List<Movie>> get() = _movieList
    val isLoading: LiveData<Boolean> get() = _isLoading
    val fail: LiveData<String> get() = _fail
    val swipe: LiveData<Unit> get() = _swipe
    val error: LiveData<StateMessage> get() = _error

    init {
        rxBind()
        getMovie("man")
    }

    private fun rxBind() {
        val movieQuery = searchButtonClick()
        val searchClick = searchMovie()
        val refresh = refreshMovie()

        Observable.merge(movieQuery, searchClick, refresh)
            .observeOn(AndroidSchedulers.mainThread())
            .map { !querySubject.value.isNullOrEmpty() }
            .subscribe { result ->
                if (result) {
                    getMovie(querySubject.value!!)
                } else {
                    showNullQuery()
                }
            }.addTo(compositeDisposable)

        movieLiked.observeOn(AndroidSchedulers.mainThread())
            .subscribe { saveMovie(it) }
            .addTo(compositeDisposable)

        moviePosition.observeOn(AndroidSchedulers.mainThread())
            .subscribe()
            .addTo(compositeDisposable)

//        /**
//         * 재시도 버튼 클릭
//         */
//        networkRetry.observeOn(AndroidSchedulers.mainThread())
//            .subscribe { networkState() }
//            .addTo(compositeDisposable)
//
//        /**
//         * 네트워크 리스너 감지
//         */
//        onAvailable.observeOn(AndroidSchedulers.mainThread())
//            .subscribe {
//                if (it == 1) {
//                    Log.d(TAG, "rxBind: 1")
//                    querySubject.value?.let { getMovie(querySubject.value!!) }
//                    setOnAvailable(0)
//                } else {
//                    Log.d(TAG, "rxBind: 0")
//                }
//            }.addTo(compositeDisposable)

        Observable.merge(networkRetry, onAvailable)
            .subscribe { getMovie(querySubject.value!!) }
            .addTo(compositeDisposable)
    }

    /**
     * 네트워크 상태 확인
     */
    private fun networkState() {
        mainRepository.getNetworkState()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doOnError { hideLoading(); }
            .doOnComplete { hideLoading(); }
            .subscribe({
                querySubject.value?.let { getMovie(it) }
            }, {
                when (it) {
                    is NetworkErrorException -> _error.value = StateMessage.NETWORK_ERROR
                    else -> _error.value = StateMessage.SERVER_ERROR
                }
            }).addTo(compositeDisposable)
    }

    /**
     * 다시 영화 가져오기
     */
    private fun retryGetMovie() {
        querySubject.value?.let {
            mainRepository.getMovies(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showLoading() }
                .doAfterTerminate { hideLoading() }
                .subscribe({ response ->
                    if (response.isEmpty()) {
                        _fail.value = "No Result"
                    } else {
                        _movieList.value = response
                    }
                    _error.value = StateMessage.NETWORK_SUCCESS
                }, { t ->
                    when (t.message) {
                        "Network Error" -> _error.value = StateMessage.NETWORK_ERROR
                        else -> _error.value = StateMessage.SERVER_ERROR
                    }
                }).addTo(compositeDisposable)
        }
    }

    /**
     *
     */
    private fun aaaa(): Observable<Unit> {
        return Observable.create {  }
    }

    private fun getMovie(query: String) {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }

        disposable = mainRepository.getMovies(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({ response ->
                if (response.isEmpty()) {
                    _fail.value = "No Result"
                } else {
                    _movieList.value = response
                }
            }, { t ->
                when (t.message) {
                    "Network Error" -> _error.value = StateMessage.NETWORK_ERROR
                    else -> _error.value = StateMessage.SERVER_ERROR
                }
            }).addTo(compositeDisposable)
    }

    fun getMoreMovies(query: String, page: Int) {
        mainRepository.getMovies(query, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({ response ->
                if (response.isNullOrEmpty()) {
                    _fail.value = "no paging data"
                } else {
                    _fail.value = "데이터를 더 불러왔습니다"
                    pagingSetMovie(response)
                }
            }, { t ->
                println(t.message)
                _fail.call()
            }).addTo(compositeDisposable)
    }

    private fun pagingSetMovie(movies: List<Movie>) {
        val newList = mutableListOf<Movie>()
        newList.addAll(_movieList.value!!)
        newList.addAll(movies)
        _movieList.value = newList
    }

    fun onSearchClick() {
        onSearchClick.onNext(Unit)
    }

    fun queryOnNext(query: String?) {
        query?.let { querySubject.onNext(query) }
    }

    fun onRetryClick() {
        Log.d(TAG, "onRetryClick:")
        networkRetry.onNext(Unit)
    }

    private fun showNullQuery() {
        _fail.value = "영화를 검색해 주세요"
    }

    fun hasLiked(movie: Movie, position: Int) {
        movieLiked.onNext(movie)
        moviePosition.onNext(position)
    }

    fun movieRefresh() {
        val item = _movieList.value

        if (item.isNullOrEmpty()) {
            _fail.value = "you dont have to refresh"
            _swipe.call()
        } else {
            movieRefresh.onNext(Unit)
        }
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

    private fun refreshMovie(): Observable<List<Movie>> {
        return movieRefresh.map { querySubject.value }
            .filter { it.isNotEmpty() }
            .throttleFirst(1000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { showLoading() }
            .switchMapSingle { query ->
                mainRepository.getMovies(query)
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { hideLoading(); _swipe.call() }
    }

    private fun saveMovie(movie: Movie) {
        val hasLiked = !movie.hasLiked
        val item = MovieLikeEntity(movie.id, hasLiked)

        val newList = mutableListOf<Movie>()
        newList.addAll(_movieList.value!!)

        val newItem = newList[moviePosition.value!!].copy(hasLiked = hasLiked)
        newList[moviePosition.value!!] = newItem

        mainRepository.saveMovieLike(item)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _movieList.value = newList
            }, { t ->
                _fail.value = t.message
            })
            .let(compositeDisposable::add)

    }

    private fun showLoading() {
        _isLoading.value = true
    }

    private fun hideLoading() {
        _isLoading.value = false
    }

    fun setOnAvailable(num: Int) {
        onAvailable.onNext(num)
    }

    fun getQuery(): String? {
        return querySubject.value
    }

    enum class StateMessage {
        NETWORK_ERROR,
        SERVER_ERROR,
        NETWORK_SUCCESS
    }
}