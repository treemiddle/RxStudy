package com.jay.rxstudyfirst.view.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.rxstudyfirst.data.signin.source.SigninRepository
import com.jay.rxstudyfirst.utils.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class SigninViewModel(
    private val signinRepository: SigninRepository
) {

    private val compositeDisposable = CompositeDisposable()

    val emailBehaviorSubject = BehaviorSubject.create<String>()
    val passwordBehaviorSubject = BehaviorSubject.create<String>()
    val confirmPublishSubject = BehaviorSubject.create<String>()

    private val _isLoading = MutableLiveData(false)
    private val _success = SingleLiveEvent<Unit>()
    private val _fail = SingleLiveEvent<Throwable>()
    private val _status = SingleLiveEvent<SignInStatus>()

    val isLoading: LiveData<Boolean> get() = _isLoading
    val success: LiveData<Unit> get() = _success
    val fail: LiveData<Throwable> get() = _fail
    val status: LiveData<SignInStatus> get() = _status

    fun onSignUp() {
        when {
            emailBehaviorSubject.value.isNullOrEmpty() -> _status.value = SignInStatus.EMPTY_EMAIL
            passwordBehaviorSubject.value.isNullOrEmpty() -> _status.value = SignInStatus.EMPTY_PASSWORD
            confirmPublishSubject.value.isNullOrEmpty() -> _status.value = SignInStatus.EMPTY_PASSWORD_CONFIRM
            else -> firebaseSignIn(emailBehaviorSubject.value!!, passwordBehaviorSubject.value!!)
        }
    }

    private fun firebaseSignIn(email: String, password: String) {
        signinRepository.signin(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doAfterTerminate { hideLoading() }
            .subscribe({
                _success.call()
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

    enum class SignInStatus {
        EMPTY_EMAIL,
        EMPTY_PASSWORD,
        EMPTY_PASSWORD_CONFIRM
    }

}