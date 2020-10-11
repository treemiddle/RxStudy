package com.jay.rxstudyfirst.utils

import android.view.View

abstract class DoubleClickListener : View.OnClickListener {

    private var lastClickTime: Long = 0

    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()

        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick()
        }

//        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
//            onDoubleClick(v)
//        } else {
//            onSingleClick(v)
//        }

        lastClickTime = clickTime
    }

    open fun onDoubleClick(){}
//    abstract fun onSingleClick(v: View?)
//    abstract fun onDoubleClick(v: View?)

    companion object {
        const val DOUBLE_CLICK_TIME_DELTA: Long = 500
    }

}