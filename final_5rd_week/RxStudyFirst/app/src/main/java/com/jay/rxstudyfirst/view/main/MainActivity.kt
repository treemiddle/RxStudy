package com.jay.rxstudyfirst.view.main

import android.content.Context
import android.net.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ActivityMainBinding
import com.jay.rxstudyfirst.utils.MyApplication
import com.jay.rxstudyfirst.utils.NetworkManager
import com.jay.rxstudyfirst.utils.activityShowToast
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter
    private lateinit var vm: MainViewModel
    private lateinit var myApplication: MyApplication
    private lateinit var network: NetworkManager
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        network = NetworkManager(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        myApplication = application as MyApplication
        vm = MainViewModel(myApplication.mainReposiroy, network)
        binding.vm = vm
        binding.lifecycleOwner = this

        initViewModelObserving()
        initAdapter()
        initRefresh()
        initTextWatcher()
    }

    private fun initViewModelObserving() {
        with(vm) {
            movieList.observe(this@MainActivity, Observer {
                adapter.submitList(it)
            })
            fail.observe(this@MainActivity, Observer {
                this@MainActivity.activityShowToast(it)
            })
            swipe.observe(this@MainActivity, Observer {
                hideRefresh()
            })
            state.observe(this@MainActivity, Observer {
                when (state.value) {
                    MainViewModel.StateMessage.NETWORK_ERROR -> {
                        snackbar = Snackbar.make((binding.parentLayout), "에러발생...", 30000)
                        snackbar?.setAction("재시도") {
                            vm.onRetryClick()
                        }?.show()
                    }
                    MainViewModel.StateMessage.UNKWON_ERROR -> {
                        this@MainActivity.activityShowToast("알수없는오류발생")
                    }
                    MainViewModel.StateMessage.NETWORK_SUCCESS -> {
                        snackbar?.dismiss()
                    }
                }
            })
        }
    }

    private fun initAdapter() {
        adapter = MainAdapter { movie, position ->
            vm.hasLiked(movie, position)
        }

        binding.recyclerView.adapter = adapter
        (binding.recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    private fun initRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            vm.movieRefresh()
        }
    }

    private fun hideRefresh() {
        binding.swipeRefresh.isRefreshing = false
    }

    private fun initTextWatcher() {
        binding.etQuery.addTextChangedListener { vm.queryOnNext(it.toString()) }
    }

}

