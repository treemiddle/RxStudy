package com.jay.rxstudyfirst.data.login.source

import com.google.firebase.auth.FirebaseUser
import com.jay.rxstudyfirst.data.login.source.remote.LoginRemoteDataSource
import io.reactivex.Single

class LoginRepositoryImpl(
    private val loginRemoteDataSource: LoginRemoteDataSource
) : LoginRepository {

    override fun login(email: String, password: String): Single<FirebaseUser> {
        return loginRemoteDataSource.login(email, password)
            .flatMap {
                Single.just(it)
            }
    }
}