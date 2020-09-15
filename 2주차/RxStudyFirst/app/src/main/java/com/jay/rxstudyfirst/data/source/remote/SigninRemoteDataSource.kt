package com.jay.rxstudyfirst.data.source.remote

import io.reactivex.Completable

interface SigninRemoteDataSource {

    fun signin(email: String, password: String): Completable
}