package com.jay.rxstudyfirst.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.rxstudyfirst.data.login.source.LoginRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

class LoginViewModel(private val loginRepository: LoginRepository) {

    private val compositeDisposable = CompositeDisposable()

    private val _isLoading = MutableLiveData(false)
    private val _fail = MutableLiveData<Throwable>()
    private val _success = MutableLiveData<Unit>()

    val isLoading: LiveData<Boolean> get() = _isLoading
    val fail: LiveData<Throwable> get() = _fail
    val success: LiveData<Unit> get() = _success

    fun firebaseLogin(email: String, password: String) {
        loginRepository.login(email, password)
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