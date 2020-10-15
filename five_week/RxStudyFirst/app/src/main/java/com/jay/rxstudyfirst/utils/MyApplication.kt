package com.jay.rxstudyfirst.utils

import android.app.Application
import android.util.Log
import com.facebook.stetho.Stetho
import com.jay.rxstudyfirst.api.ApiService
import com.jay.rxstudyfirst.data.database.JDataBase
import com.jay.rxstudyfirst.data.main.source.MainRepository
import com.jay.rxstudyfirst.data.main.source.MainRepositoryImpl
import com.jay.rxstudyfirst.data.main.source.local.MainLocalDataSourceImpl
import com.jay.rxstudyfirst.data.main.source.remote.MainRemoteDataSourceImpl
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import java.io.IOException
import java.net.SocketException

class MyApplication : Application() {
    private val TAG = javaClass.simpleName
    lateinit var mainReposiroy: MainRepository

    override fun onCreate() {
        super.onCreate()

        inject()
        initStetho()
        rxErrorHandler()
    }

    private fun inject() {
        val apiService = ApiService.api
        val database = JDataBase.Factory.create(applicationContext)
        val network = NetworkManager(applicationContext)

        val mainRemoteDataSource = MainRemoteDataSourceImpl(apiService)
        val mainLocalDataSource = MainLocalDataSourceImpl(database.movieLikeDao())

        mainReposiroy = MainRepositoryImpl(mainRemoteDataSource, mainLocalDataSource, network)
    }

    private fun initStetho() {
        Stetho.initializeWithDefaults(this)
    }

    private fun rxErrorHandler() {
        RxJavaPlugins.setErrorHandler { e ->
            var error = e
            if (error is UndeliverableException) {
                Log.d(TAG, "rxErrorHandler: 1")
                error = e.cause
            }
            if (error is IOException || error is SocketException) {
                Log.d(TAG, "rxErrorHandler: 2")
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (error is InterruptedException) {
                Log.d(TAG, "rxErrorHandler: 3")
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (error is NullPointerException || error is IllegalArgumentException) {
                // that's likely a bug in the application
                Log.d(TAG, "rxErrorHandler: 4")
                Thread.currentThread().uncaughtExceptionHandler
                    .uncaughtException(Thread.currentThread(), error)
                return@setErrorHandler
            }
            if (error is IllegalStateException) {
                Log.d(TAG, "rxErrorHandler: 5")
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler
                    .uncaughtException(Thread.currentThread(), error)
                return@setErrorHandler
            }
            Log.d(TAG, "Undeliverable exception received, not sure what to do", error)
        }
    }

}