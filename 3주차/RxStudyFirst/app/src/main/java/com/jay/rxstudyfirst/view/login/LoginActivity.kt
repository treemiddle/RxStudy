package com.jay.rxstudyfirst.view.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.data.login.source.LoginRepository
import com.jay.rxstudyfirst.data.login.source.LoginRepositoryImpl
import com.jay.rxstudyfirst.data.login.source.remote.LoginRemoteDataSource
import com.jay.rxstudyfirst.data.login.source.remote.LoginRemoteDataSourceImpl
import com.jay.rxstudyfirst.databinding.ActivityLoginBinding
import com.jay.rxstudyfirst.utils.PASSWORD_REGEX
import com.jay.rxstudyfirst.utils.activityShowToast
import com.jay.rxstudyfirst.view.main.MainActivity
import com.jay.rxstudyfirst.view.signin.SignInActivity
import com.jay.rxstudyfirst.view.tos.TermsOfUseActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_login.*
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    private val compositeDisposable = CompositeDisposable()

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var vm: LoginViewModel
    private lateinit var loginRepository: LoginRepository
    private lateinit var loginRemoteDataSource: LoginRemoteDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        auth = Firebase.auth
        loginRemoteDataSource = LoginRemoteDataSourceImpl(auth)
        loginRepository = LoginRepositoryImpl(loginRemoteDataSource)
        vm = LoginViewModel(loginRepository)

        binding.vm = vm
        binding.lifecycleOwner = this

        initViewModelObserving()
        initTextWatcher()
        regexCheck()
    }

    private fun initViewModelObserving() {
        with(vm) {
            fail.observe(this@LoginActivity, Observer {
                activityShowToast(it.message.toString())
            })
            success.observe(this@LoginActivity, Observer {
                goMain()
            })
            signup.observe(this@LoginActivity, Observer {
                goSignIn()
            })
            status.observe(this@LoginActivity, Observer {
                when (status.value) {
                    LoginViewModel.LoginStatus.EMAIL_EMPTY -> activityShowToast("이메일을 입력하세요")
                    LoginViewModel.LoginStatus.PAWORD_EMPTY -> activityShowToast("비밀번호를 입력하세요")
                }
            })
        }
    }

    private fun initTextWatcher() {
        with(vm) {
            binding.etEmail.doOnTextChanged { email, _, _, _ ->
                emailBehaviorSubject.onNext(email.toString())
            }
            binding.etPassword.doOnTextChanged { password, _, _, _ ->
                passwordBehaviorSubject.onNext(password.toString())
            }
        }
    }

    private fun regexCheck() {
        vm.emailBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .map { e -> !Patterns.EMAIL_ADDRESS.matcher(e).matches() }
            .subscribe { result -> emailPatterns(result) }
            .addTo(compositeDisposable)

        vm.passwordBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .map { p -> !Pattern.matches(PASSWORD_REGEX, p) }
            .subscribe { result -> passwordPatterns(result) }
            .addTo(compositeDisposable)

        vm._onTosClick.observeOn(AndroidSchedulers.mainThread())
            .subscribe { startActivity(Intent(this, TermsOfUseActivity::class.java)) }
            .let(compositeDisposable::add)
    }

    private fun emailPatterns(visibile: Boolean) {
        binding.etEmail.error = if (visibile) {
            getString(R.string.signin_check_email)
        } else {
            null
        }
    }

    private fun passwordPatterns(visibile: Boolean) {
        binding.etPassword.error = if (visibile) {
            getString(R.string.signin_confirm_password)
        } else {
            null
        }
    }

    private fun goSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
    }

    private fun goMain() {
        activityShowToast(getString(R.string.login_success))
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}