package com.jay.rxstudyfirst.view.tos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ActivityTermsOfUseBinding
import kotlinx.android.synthetic.main.activity_terms_of_use.*

class TermsOfUseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsOfUseBinding
    private lateinit var vm: TermsOfUseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms_of_use)
        vm = TermsOfUseViewModel()

        binding.vm = vm
        binding.lifecycleOwner = this

        initViewModelObserving()
    }

    private fun initViewModelObserving() {
        with(vm) {
            result.observe(this@TermsOfUseActivity, Observer {
                isActive(it)
            })
            button.observe(this@TermsOfUseActivity, Observer {
                goLogin()
            })
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

}