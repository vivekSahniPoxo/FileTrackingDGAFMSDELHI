package com.example.filetracking.utils

abstract class CallBack<T> {
    abstract fun onSuccess(t: T?)
    open fun onError(error: String?) {}
}




