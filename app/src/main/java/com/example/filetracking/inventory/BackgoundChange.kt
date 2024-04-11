package com.example.filetracking.inventory

import android.service.media.MediaBrowserService.BrowserRoot
import androidx.constraintlayout.widget.ConstraintLayout

interface BackgoundChange {
    fun colorChange(root:ConstraintLayout,items:FetchCategoryByIDModelClass.FetchCategoryByIDItem)

}