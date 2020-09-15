package com.jay.rxstudyfirst.data.source

import com.jay.rxstudyfirst.data.source.remote.SigninRemoteDataSource
import io.reactivex.Completable

class SigninRepositoryImpl(
    private val signinRemoteDataSource: SigninRemoteDataSource
) : SigninRepository {

    override fun signin(email: String, password: String): Completable {
        return signinRemoteDataSource.signin(email, password)
    }
}