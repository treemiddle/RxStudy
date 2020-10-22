package com.jay.rxstudyfirst.view.sixweek

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
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
    private val _email = BehaviorSubject.createDefault("")
    private val _password = BehaviorSubject.createDefault("")
    private val _confirm = BehaviorSubject.create<String>()
    private val _button = PublishSubject.create<Unit>()
    private val allCheck = Observable.combineLatest(
        emailValidator(), passwordValidator(), confirmValidator(),
        Function3 { e: Boolean, p: Boolean, c: Boolean -> Triple(e, p, c) }
    )
        .map { (e, p, c) -> e && p && c }
        .replay().autoConnect()

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
        initFormCheck(btn6, allCheck)

        val getObservable =
            _button.withLatestFrom(allCheck, BiFunction { _: Unit, x: Boolean -> x })
                .share()

        getObservables(getObservable)
    }

    private fun initTextLitener(et: EditText, subject: BehaviorSubject<String>) {
        et.addTextChangedListener { subject.onNext(it.toString()) }
    }

    private fun initButtonClick(btn: Button, subject: PublishSubject<Unit>) {
        btn.setOnClickListener { subject.onNext(Unit) }
    }

    private fun emailValidator(): Observable<Boolean> {
        return _email.skip(1)
            .map { e -> Patterns.EMAIL_ADDRESS.matcher(e).matches() }
            .doOnNext { result -> showMessage(et_6email, "email check", result) }
    }

    private fun passwordValidator(): Observable<Boolean> {
        return _password.skip(1)
            .map { p -> Pattern.matches(PASSWORD_REGEX, p) }
            .doOnNext { result -> showMessage(et_6password, "password check", result) }
    }

    private fun confirmValidator(): Observable<Boolean> {
        return _confirm.map { c -> _password.value == c }
            .doOnNext { result -> showMessage(et_6confirm, "do not same", result) }
    }

    private fun initFormCheck(btn: Button, observable: Observable<Boolean>) {
        observable.observeOn(AndroidSchedulers.mainThread())
            .subscribe { btn.isEnabled = it }
            .let(compositeDisposable::add)
    }

    private fun getObservables(observers: Observable<Boolean>) {
        observers.observeOn(AndroidSchedulers.mainThread())
            .subscribe { /* 화면 이동 */ }
            .let(compositeDisposable::add)
    }

    private fun showMessage(et: EditText, message: String, result: Boolean) {
        et.error = if (result) {
            null
        } else {
            message
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}