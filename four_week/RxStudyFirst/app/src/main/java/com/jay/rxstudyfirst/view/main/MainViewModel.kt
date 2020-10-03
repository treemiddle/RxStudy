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
import io.reactivex.rxkotlin.merge
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
    private val movieLiked = PublishSubject.create<Unit>()
    private val movieRefresh = PublishSubject.create<Unit>()

    private val _isLoading = MutableLiveData(false)
    private val _movieList = MutableLiveData<MutableList<Movie>>()
    private val _fail = SingleLiveEvent<String>()
    private val _moviePosition = MutableLiveData<Int>()
    private val _paging = MutableLiveData<List<Movie>>()
    private val _swipe = SingleLiveEvent<Unit>()

    val movieList: LiveData<MutableList<Movie>> get() = _movieList
    val isLoading: LiveData<Boolean> get() = _isLoading
    val fail: LiveData<String> get() = _fail
    val moviePosition: LiveData<Int> get() = _moviePosition
    val paging: LiveData<List<Movie>> get() = _paging
    val swipe: LiveData<Unit> get() = _swipe

    private var currentMovie: Movie? = null
    private var currentPosition: Int? = null
    private var currentHasLiked: Boolean = false

    init {
        rxBind()
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
                _movieList.value = response as MutableList<Movie>
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

        movieLiked.map { System.currentTimeMillis() }
            .buffer(2, 1)
            .map { val (first, second) = it; first to second }
            .filter { (first, second) -> second - first < 1_000 }
            .subscribe {
                currentMovie?.let {
                    it.hasLiked = !it.hasLiked
                    currentHasLiked = it.hasLiked
                    saveMovie(currentHasLiked)
                }
            }
            .addTo(compositeDisposable)

        movieRefresh.throttleFirst(1_000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map { !querySubject.value.isNullOrEmpty() }
            .subscribe{ result ->
                if (result) {
                    getMovie(querySubject.value!!)
                } else {
                    _fail.value = "불러올 데이터가 없습니다"
                }
                _swipe.call()
            }.addTo(compositeDisposable)

    }

    fun hasLiked(movie: Movie, position: Int) {
        movieLiked.onNext(Unit)
        currentMovie = movie
        currentPosition = position
    }

    fun movieRefresh() {
        movieRefresh.onNext(Unit)
    }

    fun getMoreMovies(query: String, page: Int) {
        mainRepository.getMovies(query, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({ response ->
                _paging.value = response
            }, { t ->
                _fail.value = t.message
            }).addTo(compositeDisposable)
    }

    fun getPagingQuery(): String {
        return querySubject.value.toString()
    }

    private fun showLoading() {
        _isLoading.value = true
    }

    private fun hideLoading() {
        _isLoading.value = false
    }
    
    private fun saveMovie(hasLiked: Boolean) {
        currentMovie?.let {
            val movieLike = MovieLikeEntity(it.id, hasLiked)

            mainRepository.saveMovieLike(movieLike)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _moviePosition.value = currentPosition
                }, { t ->
                    _fail.value = t.message
                })
                .let(compositeDisposable::add)
        }
    }

    //    private fun movieLiked(movie: Movie) {
//        movie.hasLiked = !movie.hasLiked
//
//        mainRepository.movieLike(movie)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { _moviePosition.value = currentPosition }
//            .addTo(compositeDisposable)
//    }

//    private fun deleteAllMovies() {
//        mainRepository.deleteAll()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { }
//            .let(compositeDisposable::add)
//    }

    //    fun getMoreMovies(page: Int) {
//        mainRepository.getMoreMovies(querySubject.value!!, page)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doOnSubscribe { showLoading() }
//            .doAfterTerminate { hideLoading() }
//            .subscribe({ response ->
//                _paging.value = response
//            }, { t ->
//                _fail.value = t.message
//            }).addTo(compositeDisposable)
//    }

}