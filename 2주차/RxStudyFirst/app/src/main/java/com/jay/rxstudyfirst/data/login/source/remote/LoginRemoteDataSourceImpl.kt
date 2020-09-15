package com.jay.rxstudyfirst.data.login.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.reactivex.Single

class LoginRemoteDataSourceImpl(
    private val auth: FirebaseAuth
) : LoginRemoteDataSource {

    override fun login(email: String, password: String): Single<FirebaseUser> {
        return Single.create { emitter ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        println("user: $user")
                        user?.let { firebaseUser ->
                            emitter.onSuccess(firebaseUser)
                        }
                    } else {
                        task.exception?.let { t ->
                            emitter.onError(t)
                        } ?: emitter.onError(Throwable(IllegalStateException("error")))
                    }
                }
                .addOnFailureListener {
                    Single.error<Throwable>(it)
                }
        }
    }
}