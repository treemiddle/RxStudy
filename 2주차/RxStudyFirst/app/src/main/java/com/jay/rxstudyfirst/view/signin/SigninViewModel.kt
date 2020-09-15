package com.jay.rxstudyfirst.view.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.rxstudyfirst.data.source.SigninRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class SigninViewModel(
    private val signinRepository: SigninRepository
) {

    private val compositeDisposable = CompositeDisposable()

    private val _isLoading = MutableLiveData(false)
    private val _success = MutableLiveData<Unit>()
    private val _fail = MutableLiveData<Throwable>()

    val isLoading: LiveData<Boolean> get() = _isLoading
    val success: LiveData<Unit> get() = _success
    val fail: LiveData<Throwable> get() = _fail

    fun firebaseSignIn(email: String, password: String) {
        signinRepository.signin(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({
                _success.value = Unit
            }, { t ->
                _fail.value = t
            }).addTo(compositeDisposable)
    }

    private fun showLoading() {
        _isLoading.value = true
    }

    private fun hideLoading() {
        _isLoading.value = false
    }

}