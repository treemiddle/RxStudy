package com.jay.rxstudyfirst.data.login.source.remote

import com.google.firebase.auth.FirebaseUser
import io.reactivex.Single

interface LoginRemoteDataSource {

    fun login(email: String, password: String): Single<FirebaseUser>
}