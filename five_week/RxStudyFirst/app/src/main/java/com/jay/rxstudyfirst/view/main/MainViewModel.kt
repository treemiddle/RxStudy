package com.jay.rxstudyfirst.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.data.MovieLikeEntity
import com.jay.rxstudyfirst.data.main.source.MainRepository
import com.jay.rxstudyfirst.utils.SingleLiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class MainViewModel(private val mainRepository: MainRepository) {

    private val TAG = javaClass.simpleName
    private val compositeDisposable = CompositeDisposable()
    private var disposable: Disposable? = null
    private val querySubject = BehaviorSubject.create<String>()
    private val onSearchClick = PublishSubject.create<Unit>()
    private val movieLiked = BehaviorSubject.create<Movie>()
    private val moviePosition = BehaviorSubject.create<Int>()
    private val movieRefresh = PublishSubject.create<Unit>()

    /**
     * 추가
     */
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

        /**
         * 추가
         */
        networkState
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { autoRetry() }
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
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({ response ->
                if (response.isEmpty()) {
                    _fail.value = "No Result"
                } else {
                    _movieList.value = response
                }
            }, { t ->
                /**
                 * 수정
                 */
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

    /**
     * 재시도 버튼 클릭
     */
    fun retryObservable() {
        val result = mainRepository.getNetwork()

        Observable.timer(2000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe {
                if (result) {
                    _state.value = StateMessage.NETWORK_SUCCESS
                    getMovie(querySubject.value!!)
                } else {
                    _state.value = StateMessage.NETWORK_ERROR
                }
            }
            .addTo(compositeDisposable)
    }

    /**
     * 네트워크 자동연결
     */
    fun networkAutoConnect() {
        networkState.onNext(true)
    }

    /**
     * 네트워크 연결로 인한 요청
     */
    private fun autoRetry() {
        Observable.timer(2000, TimeUnit.MILLISECONDS)
            .skip(0)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe {
                _state.value = StateMessage.NETWORK_SUCCESS
                getMovie(querySubject.value!!)
            }
            .addTo(compositeDisposable)
    }

    /**
     * 추가
     */
    enum class StateMessage {
        NETWORK_ERROR,
        UNKWON_ERROR,
        NETWORK_SUCCESS
    }
}