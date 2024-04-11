package com.example.filetracking.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager

class ScreenReceiver : BroadcastReceiver() {
     var iuhfService: IUHFService?=null
      var context:Context?=null
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_ON) {
            iuhfService = UHFManager.getUHFService(context)
            iuhfService?.openDev()
            iuhfService?.antennaPower = 30
            iuhfService?.inventoryStart()
        } else if (intent?.action == Intent.ACTION_SCREEN_OFF) {
            iuhfService?.closeDev()
            iuhfService?.inventoryStop()
        }
    }



}
