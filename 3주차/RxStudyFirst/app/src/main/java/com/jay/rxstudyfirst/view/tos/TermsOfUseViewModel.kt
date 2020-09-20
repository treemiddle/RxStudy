package com.jay.rxstudyfirst.view.tos

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class TermsOfUseViewModel {

    val firstCheckBox = BehaviorSubject.createDefault(false)
    val secondCheckBox = BehaviorSubject.createDefault(false)
    val threeCheckBox = BehaviorSubject.createDefault(false)

    val onButton: Subject<Unit> = PublishSubject.create()

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

}