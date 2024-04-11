package com.example.filetracking.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager

abstract class BaseDialog(context: Context) : Dialog(context) {
    protected fun setDimBlur(window: Window?) {
        if (window != null) {
            val lp = window.attributes
            lp.dimAmount = 0.5f
            window.attributes = lp
            window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}