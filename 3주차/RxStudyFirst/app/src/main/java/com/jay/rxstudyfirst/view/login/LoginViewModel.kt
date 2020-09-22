package com.jay.rxstudyfirst.view.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jay.rxstudyfirst.data.login.source.LoginRepository
import com.jay.rxstudyfirst.utils.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class LoginViewModel(private val loginRepository: LoginRepository) {

    private val compositeDisposable = CompositeDisposable()

    val emailBehaviorSubject = BehaviorSubject.createDefault("apple@apple.com")
    val passwordBehaviorSubject = BehaviorSubject.createDefault("1q2w3e4r")
    val _onTosClick = PublishSubject.create<Unit>()

    private val _isLoading = MutableLiveData(false)
    private val _fail = SingleLiveEvent<Throwable>()
    private val _success = SingleLiveEvent<Unit>()
    private val _status = SingleLiveEvent<LoginStatus>()
    private val _signup = SingleLiveEvent<Unit>()

    val isLoading: LiveData<Boolean> get() = _isLoading
    val fail: LiveData<Throwable> get() = _fail
    val success: LiveData<Unit> get() = _success
    val status: LiveData<LoginStatus> get() = _status
    val signup: LiveData<Unit> get() = _signup

    fun onLogin() {
        when {
            emailBehaviorSubject.value.isNullOrEmpty() -> _status.value = LoginStatus.EMAIL_EMPTY
            passwordBehaviorSubject.value.isNullOrEmpty() -> _status.value = LoginStatus.PAWORD_EMPTY
            else -> firebaseLogin(emailBehaviorSubject.value!!, passwordBehaviorSubject.value!!)
        }
    }

    fun onSignup() {
        _signup.call()
    }

    fun onTos() {
        _onTosClick.onNext(Unit)
    }

    private fun firebaseLogin(email: String, password: String) {
        loginRepository.login(email, password)
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

    enum class LoginStatus {
        EMAIL_EMPTY,
        PAWORD_EMPTY
    }

}