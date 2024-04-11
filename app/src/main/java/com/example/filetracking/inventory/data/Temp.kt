package com.example.filetracking.inventory.data


import com.google.gson.annotations.SerializedName

data class Temp(
    @SerializedName("category")
    val category: String,
    @SerializedName("createdBy")
    val createdBy: String,
    @SerializedName("found")
    val found: Int,
    @SerializedName("rfid")
    val rfid: List<String>,
    @SerializedName("total")
    val total: Int
)