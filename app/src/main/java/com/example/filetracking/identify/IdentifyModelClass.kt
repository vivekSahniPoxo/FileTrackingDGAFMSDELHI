package com.example.filetracking.identify


import com.google.gson.annotations.SerializedName

data class  IdentifyModelClass(
    @SerializedName("category")
    val category: Any,
    @SerializedName("cdr")
    val cdr: Any,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("isIssued")
    val isIssued: Int,
    @SerializedName("isReturned")
    val isReturned: Int,
    @SerializedName("issuedDate")
    val issuedDate: Any,
    @SerializedName("issuedTo")
    val issuedTo: Any,
    @SerializedName("rank")
    val rank: Any,
    @SerializedName("remark")
    val remark: Any,
    @SerializedName("returnDate")
    val returnDate: Any,
    @SerializedName("rfidNo")
    val rfidNo: Any
)