package com.jay.rxstudyfirst.view.main

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieLikeEntity
import com.jay.rxstudyfirst.data.main.source.MainRepository
import com.jay.rxstudyfirst.utils.NetworkManager
import com.jay.rxstudyfirst.utils.SingleLiveEvent
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.merge
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainViewModel(
    private val mainRepository: MainRepository,
    private val manager: NetworkManager
) {

    private val TAG = javaClass.simpleName
    private val compositeDisposable = CompositeDisposable()
    private var disposable: Disposable? = null
    private val querySubject = BehaviorSubject.create<String>()
    private val onSearchClick = PublishSubject.create<Unit>()
    private val movieLiked = BehaviorSubject.create<Movie>()
    private val moviePosition = BehaviorSubject.create<Int>()
    private val movieRefresh = PublishSubject.create<Unit>()
    private val retryButton = PublishSubject.create<Unit>()
    private val networkState = BehaviorSubject.create<Boolean>()

    private val _state = SingleLiveEvent<StateMessage>()
    private val _isLoading = MutableLiveData(false)
    private val _movieList = MutableLiveData<List<Movie>>()
    private val _fail = SingleLiveEvent<String>()
    private val _swipe = SingleLiveEvent<Unit>()

    val movieList: LiveData<List<Movie>> get() = _movieList
    val isLoading: LiveData<Boolean> get() = _isLoading
    val fail: LiveData<String> get() = _fail
    val swipe: LiveData<Unit> get() = _swipe
    val state: LiveData<StateMessage> get() = _state

    init {
        rxBind()
    }

    private fun getNetworkState(): Flowable<Boolean>{
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        return Flowable.create({ emitter: FlowableEmitter<Boolean> ->
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    emitter.onNext(true)
                }

                override fun onLost(network: Network) {
                    emitter.onNext(false)
                }
            }
            manager.register(request, callback)
            emitter.setCancellable {
                manager.unregister(callback)
            }
        }, BackpressureStrategy.LATEST)
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


//        Observable.merge(retryButton, networkState)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { getMovie(querySubject.value!!) }
//            .addTo(compositeDisposable)

        retryButton.observeOn(AndroidSchedulers.mainThread())
            .subscribe { retryFlowable() }
            .addTo(compositeDisposable)

        networkState.observeOn(AndroidSchedulers.mainThread())
            .subscribe { networkFlowable(it) }
            .addTo(compositeDisposable)
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
            .retryWhen { retryWhen ->
                Log.d(TAG, "retryWhen: ")
                retryWhen.flatMap { e ->
                    Log.d(TAG, "flatMap: ")
                    _state.value = StateMessage.NETWORK_ERROR
                    Flowable.ambArray(
                        getNetworkState()
                            .filter{ isConnected ->  isConnected },
                        retryButton.toFlowable(BackpressureStrategy.DROP)
                    ).doOnNext { _state.value = StateMessage.NETWORK_SUCCESS }
                }
            }
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({ response ->
                if (response.isEmpty()) {
                    _fail.value = "No Result"
                } else {
                    _movieList.value = response
                }
            }, { t ->
                Log.d(TAG, "getMovie fail: $t")
                when (t.message) {
                    "Network Error" -> _state.value = StateMessage.NETWORK_ERROR
                    else -> _state.value = StateMessage.UNKWON_ERROR
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
        retryButton.onNext(Unit)
    }

    fun onNetworkState(state: Boolean) {
        networkState.onNext(state)
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

    private fun networkFlowable(state: Boolean): Flowable<Boolean> {
        return Flowable.create({ emitter ->
            Log.d(TAG, "networkFlowable: ")
//            networkState.observeOn(AndroidSchedulers.mainThread())
//                .subscribe { emitter.onNext(it) }
            emitter.onNext(state)
        }, BackpressureStrategy.DROP)
    }

    private fun retryFlowable(): Flowable<Unit> {
        return Flowable.create({ emitter ->
            Log.d(TAG, "retryFlowable: ")
//            retryButton.observeOn(AndroidSchedulers.mainThread())
//                .subscribe { emitter.onNext(Unit) }
            emitter.onNext(Unit)
        }, BackpressureStrategy.DROP)
    }

    enum class StateMessage {
        NETWORK_ERROR,
        UNKWON_ERROR,
        NETWORK_SUCCESS
    }

}