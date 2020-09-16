package com.jay.rxstudyfirst.data.signin.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.jay.rxstudyfirst.data.signin.source.remote.SigninRemoteDataSource
import io.reactivex.Completable

class SigninRemoteDataSourceImpl(
    private val auth: FirebaseAuth
) : SigninRemoteDataSource {

    override fun signin(email: String, password: String): Completable {
        return Completable.create { emitter ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        emitter.onComplete()
                    } else {
                        emitter.onError(Throwable(task.exception))
                    }
                }
        }
    }
}