package com.jay.rxstudyfirst.view.signin

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ActivitySignInBinding
import com.jay.rxstudyfirst.utils.activityShowToast
import com.jay.rxstudyfirst.view.login.LoginActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import java.util.regex.Pattern

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val compositeDisposable = CompositeDisposable()
    private val emailBehaviorSubject = PublishSubject.create<String>()
    private val passwordBehaviorSubject = PublishSubject.create<String>()
    private val confirmBehaviorSubject = PublishSubject.create<String>()
    private val passwordRegex = "^[a-zA-Z0-9]{6,}$"
    private var emailFlag = false
    private var passwordFlag = false
    private var confirmFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        initClickListener()
        initTextWatcher()
        regexCheck()
    }

    private fun initClickListener() {
        binding.btnSignin.setOnClickListener {
            if (emailFlag && passwordFlag && confirmFlag) {
                activityShowToast("성공")
            } else {
                activityShowToast("실패")
            }
        }
    }

    private fun initTextWatcher() {
        with(binding) {
            etEmail.doOnTextChanged { email, _, _, _ ->
                emailBehaviorSubject.onNext(email.toString())
            }
            etPassword.doOnTextChanged { password, _, _, _ ->
                passwordBehaviorSubject.onNext(password.toString())
            }
            etPasswordConfirm.doOnTextChanged { confirm, _, _, _ ->
                confirmBehaviorSubject.onNext(confirm.toString())
            }
        }
    }

    private fun regexCheck() {
        emailBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe { email ->
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailFlag = false
                    showErrorEmailPatterns()
                } else {
                    emailFlag = true
                }
            }.addTo(compositeDisposable)

        passwordBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe { password ->
                if (!Pattern.matches(passwordRegex, password)) {
                    passwordFlag = false
                    showErrorPasswordPatterns()
                } else {
                    passwordFlag = true
                }
            }.addTo(compositeDisposable)

        confirmBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe { confirm ->
                if (binding.etPassword.text.toString() != confirm) {
                    confirmFlag = false
                    showFailPassword()
                } else {
                    confirmFlag = true
                }
            }.addTo(compositeDisposable)
    }

    private fun showErrorEmailPatterns() {
        binding.etEmail.error = "이메일을 확인해 주세요"
    }

    private fun showErrorPasswordPatterns() {
        binding.etPassword.error = "비밀번호를 확인해 주세요"
    }

    private fun showFailPassword() {
        binding.etPasswordConfirm.error = "비밀번호가 틀립니다"
    }

    private fun successSignIn() {
        val successIntent = Intent(this, LoginActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        startActivity(successIntent)
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}