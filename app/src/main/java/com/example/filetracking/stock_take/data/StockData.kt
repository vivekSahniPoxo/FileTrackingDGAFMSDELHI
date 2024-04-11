package com.example.filetracking.stock_take.data

import com.example.filetracking.inventory.data.RfidItem
import com.google.gson.annotations.SerializedName

//data class StockData(
//    @SerializedName("found")
//    val found: Int,
//    @SerializedName("notFound")
//    val notFound: Int,
//    @SerializedName("stockDate")
//    val stockDate: String,
//    @SerializedName("stockId")
//    val stockId: Int,
//    @SerializedName("stockTakes")
//    val stockTakes: List<StockTake>,
//    @SerializedName("total")
//    val total: Int
//) {
//    data class StockTake(
//        @SerializedName("crdName")
//        val crdName: String,
//        @SerializedName("rfidNo")
//        val rfidNo: String,
//        @SerializedName("status")
//        val status: String
//    )
//}

data class StockData(
    @SerializedName("createdBy")
    val createdBy: String,
    @SerializedName("listofrfid")
    val rfid: List<RfidItem>
)


//data class StockData(
//    @SerializedName("createdBy")
//    val createdBy:String,
//    @SerializedName("stockTakes")
//    val stockTakes: List<StockTake>,
//) {
//    data class StockTake(
//
//        @SerializedName("rfidNo")
//        val rfidNo: String,
//    )
//}

