package com.jay.rxstudyfirst.view.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.data.Movie
import com.jay.rxstudyfirst.databinding.ActivityMainBinding
import com.jay.rxstudyfirst.utils.MyApplication
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.internal.Internal.instance

class MainActivity : AppCompatActivity() {

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
                toastMessage(it)
            })
            swipe.observe(this@MainActivity, Observer {
                hideRefresh()
            })
            check.observe(this@MainActivity, Observer {
                println("??????????")
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("zz")
                    .setPositiveButton("ok") { _, _ ->
                        vm.a()
                    }
                    .setNegativeButton("cancle") { _, _->

                    }
                    .show()

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

    private fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}

