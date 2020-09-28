package com.jay.rxstudyfirst.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jay.rxstudyfirst.R

fun ImageView.loadImage(iv: ImageView, url: String?) {
    if (url == null) {
        Glide.with(iv)
            .load(R.drawable.ic_error)
            .apply {
                RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
            }
            .into(iv)
    } else {
        Glide.with(iv)
            .load(url)
            .apply {
                RequestOptions()
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
            }
            .into(iv)
    }
}