package com.jay.rxstudyfirst.data.login.source

import com.google.firebase.auth.FirebaseUser
import io.reactivex.Single

interface LoginRepository {

    fun login(email: String, password: String): Single<FirebaseUser>
}