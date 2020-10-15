package com.jay.rxstudyfirst.view.main

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ActivityMainBinding
import com.jay.rxstudyfirst.utils.MyApplication
import com.jay.rxstudyfirst.utils.activityShowToast
import io.reactivex.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {
    private val TAG = javaClass.simpleName
    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter
    private lateinit var vm: MainViewModel
    private lateinit var myApplication: MyApplication
    private var snackbar: Snackbar? = null
    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        myApplication = application as MyApplication
        vm = MainViewModel(myApplication.mainReposiroy)
        binding.vm = vm
        binding.lifecycleOwner = this

        inject()
        initView()
        initViewModelObserving()
        initAdapter()
        initRefresh()
        initTextWatcher()
    }

    private fun inject() {
        myApplication = application as MyApplication
        vm = MainViewModel(myApplication.mainReposiroy)
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = vm
        binding.lifecycleOwner = this
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
            /**
             * 상태값 추가
             */
            state.observe(this@MainActivity, Observer {
                when (state.value) {
                    MainViewModel.StateMessage.NETWORK_ERROR -> {
//                        binding.parentLayout.snackbar("재시도", object : MergeInterface.SnackbarListener {
//                            override fun onRetry() {
//                                vm.retryObservable()
//                            }
//                        })
                        snackbar = Snackbar.make((binding.parentLayout), "에러발생...", 30000)
                        snackbar?.setAction("재시도") { vm.retryObservable() }?.show()
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

    /**
     * 깜빡 거림 현상 해결해야됨....
     */
    private fun initAdapter() {
        adapter = MainAdapter { movie, position ->
            vm.hasLiked(movie, position)
        }

        binding.recyclerView.adapter = adapter
        //(binding.recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
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

    /**
     * ↓ 밑으로 전부 추가
     */
    override fun onStart() {
        super.onStart()
        registerNetwork()
    }

    override fun onStop() {
        super.onStop()
        unregisterNetwork()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    /**
     * 인터넷 연결 될 떄마다 호출됨.....
     */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            count++
            if (count in 0..1) {
                Log.d(TAG, "onAvailable nonono: $count")
            } else {
                Log.d(TAG, "onAvailable gogo: $count")
                vm.networkAutoConnect()
            }
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "onLost: ")
        }
    }

    private fun registerNetwork() {
        val connectManager = getSystemService(ConnectivityManager::class.java)
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectManager.registerNetworkCallback(request, networkCallback)
    }

    private fun unregisterNetwork() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

}

