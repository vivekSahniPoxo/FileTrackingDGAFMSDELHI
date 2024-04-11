package com.example.filetracking.inventory.data

import com.google.gson.annotations.SerializedName

data class InventoryDataClass(
    @SerializedName("category")
    val category: String,
    @SerializedName("createdBy")
    val createdBy: String,
    @SerializedName("found")
    val found: Int,
    @SerializedName("listofrfid")
    val rfid: List<RfidItem>,
    @SerializedName("total")
    val total: Int)

data class RfidItem(val rfidno: String, var status: String)




//    @SerializedName("found")
//    val found: Int,
//    @SerializedName("stockTakes")
//    val rfidNumber: List<Rfid>,
//    @SerializedName("total")
//    val total: Int,
//    @SerializedName("createdBy")
//    val createdBy:String,
//    @SerializedName("category")
//    val category: String
//
//) {
//    data class Rfid(
//        @SerializedName("rfidNo")
//        val rfidNo: String
//    )
//}