package com.example.filetracking.stock_take.data


import com.google.gson.annotations.SerializedName

data class TempTo(
    @SerializedName("createdBy")
    val createdBy: String,
    @SerializedName("rfid")
    val rfid: List<String>
)