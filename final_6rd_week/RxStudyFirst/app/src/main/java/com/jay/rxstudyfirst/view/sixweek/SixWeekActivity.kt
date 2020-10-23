package com.jay.rxstudyfirst.view.sixweek

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.utils.PASSWORD_REGEX
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_sixweek.*
import java.util.regex.Pattern

class SixWeekActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val _email = BehaviorSubject.create<String>()
    private val _password = BehaviorSubject.create<String>()
    private val _confirm = BehaviorSubject.create<String>()
    private val _button = PublishSubject.create<Unit>()
    private val allCheck = Observable.combineLatest(
        emailValidator(), passwordValidator(), confirmValidator(),
        Function3 { e: Boolean, p: Boolean, c: Boolean -> Triple(e, p, c) }
    )
        .map { (e, p, c) -> e && p && c }
        .replay(1).autoConnect()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sixweek)

        bindRx()
        initTextLitener(et_6email, _email)
        initTextLitener(et_6password, _password)
        initTextLitener(et_6confirm, _confirm)
        initButtonClick(btn6, _button)
    }

    private fun bindRx() {
        _button.observeOn(AndroidSchedulers.mainThread())
            .subscribe { gogogogogo() }
            .let(compositeDisposable::add)

        initFormCheck(btn6, allCheck)
    }

    private fun initTextLitener(et: EditText, subject: BehaviorSubject<String>) {
        et.addTextChangedListener { subject.onNext(it.toString()) }
    }

    private fun emailValidator(): Observable<Boolean> {
        return _email.map { e -> Patterns.EMAIL_ADDRESS.matcher(e).matches() }
            .doOnNext { showMessage(et_6email, "email check", it) }
    }

    private fun passwordValidator(): Observable<Boolean> {
        return _password.map { p -> Pattern.matches(PASSWORD_REGEX, p) }
            .doOnNext { showMessage(et_6password, "password check", it) }
    }

    private fun confirmValidator(): Observable<Boolean> {
        return Observable.combineLatest(_password, _confirm, BiFunction { p: String, c: String -> p == c })
            .doOnNext { showMessage(et_6confirm, "check", it) }
    }

    private fun initButtonClick(btn: Button, subject: PublishSubject<Unit>) {
        btn.setOnClickListener { subject.onNext(Unit) }
    }

    private fun initFormCheck(btn: Button, observable: Observable<Boolean>) {
        observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe { btn.isEnabled = it }
            .let(compositeDisposable::add)
    }

    private fun showMessage(et: EditText, message: String, result: Boolean) {
        et.error = if (result) {
            null
        } else {
            message
        }
    }

    private fun gogogogogo() {
        Toast.makeText(this, "gogogo", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}