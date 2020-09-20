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
import com.jay.rxstudyfirst.data.signin.source.SigninRepository
import com.jay.rxstudyfirst.data.signin.source.SigninRepositoryImpl
import com.jay.rxstudyfirst.data.signin.source.remote.SigninRemoteDataSource
import com.jay.rxstudyfirst.data.signin.source.remote.SigninRemoteDataSourceImpl
import com.jay.rxstudyfirst.utils.PASSWORD_REGEX
import com.jay.rxstudyfirst.utils.activityShowToast
import com.jay.rxstudyfirst.view.login.LoginActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.util.regex.Pattern

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val compositeDisposable = CompositeDisposable()

    private lateinit var auth: FirebaseAuth
    private lateinit var vm: SigninViewModel
    private lateinit var signinRepository: SigninRepository
    private lateinit var signinRemoteDataSource: SigninRemoteDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)
        auth = Firebase.auth
        signinRemoteDataSource = SigninRemoteDataSourceImpl(auth)
        signinRepository = SigninRepositoryImpl(signinRemoteDataSource)
        vm = SigninViewModel(signinRepository)

        binding.vm = vm
        binding.lifecycleOwner = this

        initViewModelObserving()
        initTextWatcher()
        regexCheck()
    }

    private fun initViewModelObserving() {
        with(vm) {
            success.observe(this@SignInActivity, Observer {
                successSignIn()
            })
            fail.observe(this@SignInActivity, Observer {
                activityShowToast(it.message.toString())
            })
            status.observe(this@SignInActivity, Observer {
                when (status.value) {
                    SigninViewModel.SignInStatus.EMPTY_EMAIL -> activityShowToast("이메일 입력하세요")
                    SigninViewModel.SignInStatus.EMPTY_PASSWORD -> activityShowToast("비밀번호 입력하세요")
                    SigninViewModel.SignInStatus.EMPTY_PASSWORD_CONFIRM -> activityShowToast("비밀번호 확인하세요")
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
            binding.etPasswordConfirm.doOnTextChanged { password, _, _, _ ->
                confirmPublishSubject.onNext(password.toString())
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

        vm.confirmPublishSubject.observeOn(AndroidSchedulers.mainThread())
            .subscribe { confirm ->
                if (vm.passwordBehaviorSubject.value != confirm) {
                    showPasswordCheck(true)
                } else {
                    showPasswordCheck(false)
                }
            }.addTo(compositeDisposable)
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

    private fun showPasswordCheck(visibile: Boolean) {
        binding.etPasswordConfirm.error = if (visibile) {
            getString(R.string.signin_fail_password)
        } else {
            null
        }
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