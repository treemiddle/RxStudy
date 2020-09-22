package com.jay.rxstudyfirst.view.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.api.ApiInterface
import com.jay.rxstudyfirst.api.ApiService
import com.jay.rxstudyfirst.data.main.source.MainRepository
import com.jay.rxstudyfirst.data.main.source.MainRepositoryImpl
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSource
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSourceImpl
import com.jay.rxstudyfirst.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter
    private lateinit var vm: MainViewModel
    private lateinit var repository: MainRepository
    private lateinit var remote: MainRemoteDataSource
    private lateinit var service: ApiInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initViewModelObserving()
        initAdapter()
        initTextWatcher()
    }

    private fun initView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        service = ApiService.api
        remote = MainRemoteDataSourceImpl(service)
        repository = MainRepositoryImpl(remote)
        vm = MainViewModel(repository)

        binding.vm = vm
        binding.lifecycleOwner = this
    }

    private fun initViewModelObserving() {
        with(vm) {
            movieList.observe(this@MainActivity, Observer {
                adapter.setMovieItem(it)
            })
            fail.observe(this@MainActivity, Observer {
                toastMessage(it)
            })
        }
    }

    private fun initAdapter() {
        adapter = MainAdapter { movie ->
            toastMessage("$movie")
        }

        binding.recyclerView.adapter = adapter
    }

    private fun initTextWatcher() {
        binding.etQuery.addTextChangedListener { vm.queryOnNext(it.toString()) }
    }

    private fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}

