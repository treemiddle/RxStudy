package com.jay.rxstudyfirst.view.tos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ActivityTermsOfUseBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.activity_terms_of_use.*

class TermsOfUseActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: ActivityTermsOfUseBinding
    private lateinit var vm: TermsOfUseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms_of_use)
        vm = TermsOfUseViewModel()

        binding.vm = vm
        binding.lifecycleOwner = this

        vmRxBind()
    }

    private fun vmRxBind() {
        with(vm) {
            Observable.combineLatest(
                firstCheckBox,
                secondCheckBox,
                threeCheckBox,
                Function3 { t1: Boolean, t2: Boolean, t3: Boolean -> Triple(t1, t2, t3) }
            )
                .map { (t1, t2, t3) -> t1 && t2 && t3 }
                .subscribe { isActive(it) }
                .let(compositeDisposable::add)

            onButton.observeOn(AndroidSchedulers.mainThread())
                .subscribe { goLogin() }
                .let(compositeDisposable::add)
        }
    }

    private fun isActive(result: Boolean) {
        if (result) {
            cb_four.isChecked = true
            btn_check.isEnabled = true
        } else {
            cb_four.isChecked = false
            btn_check.isEnabled = false
        }
    }

    private fun goLogin() {
        finish()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }


}