package com.example.filetracking.inventory


import com.google.gson.annotations.SerializedName

class FetchCategoryByIDModelClass : ArrayList<FetchCategoryByIDModelClass.FetchCategoryByIDItem>(){
    data class FetchCategoryByIDItem(
        @SerializedName("category")
        val category: String,
        @SerializedName("cdr")
        val cdr: String,
        @SerializedName("createdAt")
        val createdAt: String,
        @SerializedName("id")
        val id: Int,
        @SerializedName("isIssued")
        val isIssued: Int,
        @SerializedName("isReturned")
        val isReturned: Int,
        @SerializedName("issuedDate")
        val issuedDate: String,
        @SerializedName("issuedTo")
        val issuedTo: String,
        @SerializedName("rank")
        val rank: String,
        @SerializedName("remark")
        val remark: String,
        @SerializedName("returnDate")
        val returnDate: String,
        @SerializedName("rfidNo")
        val rfidNo: String,
        var status:String
    )
}


data class rfidNo(val rfidNo:String)


