package com.jay.rxstudyfirst.utils

interface MergeInterface {

    interface OnLoadMoreListener {
        fun onLoadMore(page: Int)
    }

    interface SnackbarListener {
        fun onRetry()
    }
}