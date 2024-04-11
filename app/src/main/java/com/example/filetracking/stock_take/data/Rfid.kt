package com.example.filetracking.stock_take.data

import android.os.Parcelable


data class Rfid(val rfid:String)

data class RfidDetails(val rfid:String,val file:String,val category: String,val rank:String)

