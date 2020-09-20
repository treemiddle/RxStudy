package com.jay.rxstudyfirst.view.main

import com.jay.rxstudyfirst.data.main.source.MainRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainViewModel(private val mainRepository: MainRepository) {

    private val compositeDisposable = CompositeDisposable()

    fun test(query: String) {
        mainRepository.getMovie(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError { println("doonerror: ${it.message}") }
            .doOnSubscribe {  }
            .doAfterTerminate {  }
            .subscribe({
                println("result: $it")
            }, { t ->
                println("fail: ${t.message}")
            }).let(compositeDisposable::add)
    }
}