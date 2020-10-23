package com.jay.rxstudyfirst.utils

import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

abstract class DoubleClickListener : View.OnClickListener, AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val _click = PublishSubject.create<Unit>()

    init {
        onItemClick()
    }

    override fun onClick(v: View) {
        _click.onNext(Unit)
    }

    abstract fun onDoubleClick()

    private fun onItemClick() {
        _click.map { System.currentTimeMillis() }
            .buffer(2, 1)
            .observeOn(AndroidSchedulers.mainThread())
            .map { val (first, second) = it; first to second }
            .filter { (first, second) -> second - first < 500 }
            .subscribe { onDoubleClick() }
            .let(compositeDisposable::add)
    }

    override fun onDestroy() {
        Log.d("Main", "onDestroy: dddddd")
        compositeDisposable.clear()
        super.onDestroy()
    }

}