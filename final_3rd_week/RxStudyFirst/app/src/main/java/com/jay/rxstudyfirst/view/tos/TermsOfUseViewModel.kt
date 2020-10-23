package com.jay.rxstudyfirst.view.tos

import androidx.lifecycle.LiveData
import com.jay.rxstudyfirst.utils.SingleLiveEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class TermsOfUseViewModel {

    private val compositeDisposable = CompositeDisposable()
    private val firstCheckBox = BehaviorSubject.createDefault(false)
    private val secondCheckBox = BehaviorSubject.createDefault(false)
    private val threeCheckBox = BehaviorSubject.createDefault(false)
    private val onButton: Subject<Unit> = PublishSubject.create()

    private val _result = SingleLiveEvent<Boolean>()
    private val _button = SingleLiveEvent<Unit>()

    val result: LiveData<Boolean> get() = _result
    val button: LiveData<Unit> get() = _button

    init {
        rxBind()
    }

    fun firstClick() {
        with(firstCheckBox) {
            if (this.value!!) {
                onNext(false)
            } else {
                onNext(true)
            }
        }
    }

    fun secondClick() {
        with(secondCheckBox) {
            if (this.value!!) {
                onNext(false)
            } else {
                onNext(true)
            }
        }
    }

    fun threeClick() {
        with(threeCheckBox) {
            if (this.value!!) {
                onNext(false)
            } else {
                onNext(true)
            }
        }
    }

    fun onButton() {
        onButton.onNext(Unit)
    }

    private fun rxBind() {
        Observable.combineLatest(
            firstCheckBox,
            secondCheckBox,
            threeCheckBox,
            Function3 { t1: Boolean, t2: Boolean, t3: Boolean -> Triple(t1, t2, t3) }
        )
            .map { (t1, t2, t3) -> t1 && t2 && t3 }
            .subscribe { _result.value = it }
            .addTo(compositeDisposable)

        onButton.observeOn(AndroidSchedulers.mainThread())
            .subscribe { _button.call() }
            .addTo(compositeDisposable)
    }

}