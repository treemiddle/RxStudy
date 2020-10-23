package com.jay.rxstudyfirst.data.signin.source

import com.jay.rxstudyfirst.data.signin.source.remote.SigninRemoteDataSource
import io.reactivex.Completable

class SigninRepositoryImpl(
    private val signinRemoteDataSource: SigninRemoteDataSource
) : SigninRepository {

    override fun signin(email: String, password: String): Completable {
        return signinRemoteDataSource.signin(email, password)
    }
}