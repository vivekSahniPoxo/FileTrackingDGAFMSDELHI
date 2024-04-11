package com.example.filetracking.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.example.filetracking.R



import com.google.android.material.button.MaterialButton

class InternetConnectionDialog(context: Context) : BaseDialog(context) {

    constructor(context: Context, funValue: Int, callback: CallBack<Int>?) : this(context) {
        mCtx = context
        action = funValue
        callBack = callback
        isRetried = false
    }

    constructor(context: Context, callback: CallBack<Int>?) : this(context) {
        mCtx = context
        action = Cons.NO_ACTION
        callBack = callback
        isRetried = false
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var mCtx: Context
        var action: Int? = null
        var callBack: CallBack<Int>? = null
        var isRetried = false

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_no_internet_conntection)
        setDimBlur(window)
        setCancelable(false)
        val retry:MaterialButton=findViewById(R.id.btRetry)
        retry.setOnClickListener {
            isRetried = true
            this@InternetConnectionDialog.dismiss()
        }


    }
}
