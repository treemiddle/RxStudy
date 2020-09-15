package com.jay.rxstudyfirst.view.signin

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
import com.jay.rxstudyfirst.data.signin.source.SigninRepository
import com.jay.rxstudyfirst.data.signin.source.SigninRepositoryImpl
import com.jay.rxstudyfirst.data.signin.source.remote.SigninRemoteDataSource
import com.jay.rxstudyfirst.data.signin.source.remote.SigninRemoteDataSourceImpl
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

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: SigninViewModel
    private lateinit var signinRepository: SigninRepository
    private lateinit var signinRemoteDataSource: SigninRemoteDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        auth = Firebase.auth
        signinRemoteDataSource = SigninRemoteDataSourceImpl(auth)
        signinRepository = SigninRepositoryImpl(signinRemoteDataSource)
        viewModel = SigninViewModel(signinRepository)

        binding.vm = viewModel
        binding.lifecycleOwner = this

        initViewModelObserving()
        initClickListener()
        initTextWatcher()
        regexCheck()
    }

    private fun initClickListener() {
        with(binding) {
            btnSignin.setOnClickListener {
                viewModel.firebaseSignIn(etEmail.text.toString(), etPassword.text.toString())
            }
        }
    }

    private fun initViewModelObserving() {
        with(viewModel) {
            success.observe(this@SignInActivity, Observer {
                successSignIn()
            })
            fail.observe(this@SignInActivity, Observer {
                activityShowToast(it.message.toString())
            })
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
                    showErrorEmailPatterns()
                }
            }.addTo(compositeDisposable)

        passwordBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe { password ->
                if (!Pattern.matches(passwordRegex, password)) {
                    showErrorPasswordPatterns()
                }
            }.addTo(compositeDisposable)

        confirmBehaviorSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe { confirm ->
                if (binding.etPassword.text.toString() != confirm) {
                    showFailPassword()
                }
            }.addTo(compositeDisposable)
    }

    private fun showErrorEmailPatterns() {
        binding.etEmail.error = getString(R.string.signin_check_email)
    }

    private fun showErrorPasswordPatterns() {
        binding.etPassword.error = getString(R.string.signin_confirm_password)
    }

    private fun showFailPassword() {
        binding.etPasswordConfirm.error = getString(R.string.signin_fail_password)
    }

    private fun successSignIn() {
        activityShowToast(getString(R.string.sigin_success))
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