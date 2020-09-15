package com.jay.rxstudyfirst.data.source

import io.reactivex.Completable

interface SigninRepository {

    fun signin(email: String, password: String): Completable
}