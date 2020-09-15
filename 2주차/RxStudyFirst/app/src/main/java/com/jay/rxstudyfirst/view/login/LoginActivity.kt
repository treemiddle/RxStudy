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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private val emailBehaviorSubject = BehaviorSubject.create<String>()
    private val passwordBehaviorSubject = BehaviorSubject.create<String>()

    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: LoginViewModel
    private lateinit var loginRepository: LoginRepository
    private lateinit var loginRemoteDataSource: LoginRemoteDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        auth = Firebase.auth
        loginRemoteDataSource = LoginRemoteDataSourceImpl(auth)
        loginRepository = LoginRepositoryImpl(loginRemoteDataSource)
        viewModel = LoginViewModel(loginRepository)

        binding.vm = viewModel
        binding.lifecycleOwner = this

        initViewModelObserving()
        initClickListener()
        initTextWatcher()
        rxBind()
    }

    private fun initViewModelObserving() {
        with(viewModel) {
            fail.observe(this@LoginActivity, Observer {
                activityShowToast(it.message.toString())
            })
            success.observe(this@LoginActivity, Observer {
                goMain()
            })
        }
    }

    private fun initClickListener() {
        with(binding) {
            btnLogin.setOnClickListener {
                viewModel.firebaseLogin(etEmail.text.toString(), etPassword.text.toString())
            }
            btnSignin.setOnClickListener { goSignIn() }
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
        }
    }

    private fun rxBind() {
        emailBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe { email ->
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showErrorEmailPatterns()
                }
            }.addTo(compositeDisposable)

        passwordBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe { password ->
                if (!Pattern.matches(PASSWORD_REGEX, password)) {
                    showErrorPasswordPatterns()
                }
            }.addTo(compositeDisposable)
    }

    private fun showErrorEmailPatterns() {
        binding.etEmail.error = getString(R.string.signin_check_email)
    }

    private fun showErrorPasswordPatterns() {
        binding.etPassword.error = getString(R.string.signin_confirm_password)
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