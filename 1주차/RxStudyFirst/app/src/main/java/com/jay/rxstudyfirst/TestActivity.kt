package com.jay.rxstudyfirst

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_test.*
import java.util.concurrent.TimeUnit

class TestActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val _password: Subject<String> = PublishSubject.create()
    private val _passwordConfirm: Subject<String> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        rxBind()
    }

    private fun rxBind() {
        et_password.doOnTextChanged { password, _, _, _ -> _password.onNext(password.toString()) }
        et_password_confirm.addTextChangedListener { pw -> _passwordConfirm.onNext(pw.toString()) }

        Observable.combineLatest(
            _password,
            _passwordConfirm,
            BiFunction { p: String, c: String -> p to c }
        )
            .map { (p, c) -> p == c }
            .subscribe {
                showPassword(it)
            }
            .let(compositeDisposable::add)
    }

    private fun showPassword(visibile: Boolean) {
        et_password.error = if (visibile) {
            null
        } else {
            "비밀번호를 확인해 주세요"
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}