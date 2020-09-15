package com.jay.rxstudyfirst.data.source.remote

import com.google.firebase.auth.FirebaseAuth
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
                .addOnFailureListener { error ->
                    emitter.onError(error)
                }
        }
    }
}