package com.jay.rxstudyfirst.view.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ActivityMainBinding
import com.jay.rxstudyfirst.utils.MergeInterface
import com.jay.rxstudyfirst.utils.MyApplication
import com.jay.rxstudyfirst.utils.activityShowToast
import com.jay.rxstudyfirst.utils.snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter
    private lateinit var vm: MainViewModel
    private lateinit var myApplication: MyApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            error.observe(this@MainActivity, Observer {
                when (error.value) {
                    MainViewModel.StateMessage.NETWORK_ERROR -> binding.parentLayout.snackbar("에러가 발생했어요...",
                        object : MergeInterface.SnackbarListener {
                            override fun onRetry() {
                                Observable.just(Unit)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnSubscribe { binding.progressBar.visibility = View.VISIBLE }
                                    .delay(3000, TimeUnit.MILLISECONDS)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnTerminate { binding.progressBar.visibility = View.GONE }
                                    .subscribe { vm.getMovie("man") }
                                    .let(compositeDisposable::add)
                            }
                        })
                    MainViewModel.StateMessage.SERVER_ERROR -> this@MainActivity.activityShowToast("알 수 없어요....")
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

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

}

