package com.jay.rxstudyfirst.data.signin.source.remote

import io.reactivex.Completable

interface SigninRemoteDataSource {

    fun signin(email: String, password: String): Completable
}