package com.example.filetracking.issue_file


import com.google.gson.annotations.SerializedName

//data class InternalBulkMovement(
//    @SerializedName("action")
//    val action: String,
//    @SerializedName("description")
//    val description: String,
//    @SerializedName("listofrfid")
//    val listofrfid: List<Listofrfid>
//) {
//    data class Listofrfid(
//        @SerializedName("rfidno")
//        val rfidno: String,
//        @SerializedName("status")
//        val status: String
//    )
//}


data class RfidItemList(val rfidno: String, val status: String)

data class RfidRequest(val listofrfid: List<RfidItemList>, val action: String, val description: String,val name:String)

