package com.jay.rxstudyfirst.utils

import android.app.Activity
import android.widget.Toast
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

fun <T> Call<T>.rxSingle(): Single<T> {
    return Single.create { emitter ->
        this.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                emitter.onError(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    emitter.onSuccess(body)
                } else {
                    emitter.onError(HttpException(response))
                }
            }
        })

        emitter.setCancellable { this.cancel() }
    }
}

fun <T> Call<T>.rxMaybe(): Maybe<T> {
    return Maybe.create { emitter ->
        this.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                emitter.onError(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                when (response.isSuccessful) {
                    true -> body?.let {
                        emitter.onSuccess(it)
                    } ?: emitter.onComplete()
                    false -> emitter.onError(HttpException(response))
                }
            }
        })

        emitter.setCancellable { this.cancel() }
    }
}

fun <T> Call<T>.rxCompletable(): Completable {
    return Completable.create { emitter ->
        this.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                emitter.onError(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    emitter.onComplete()
                } else {
                    emitter.onError(HttpException(response))
                }
            }
        })

        emitter.setCancellable { this.cancel() }
    }
}

fun Activity.activityShowToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}