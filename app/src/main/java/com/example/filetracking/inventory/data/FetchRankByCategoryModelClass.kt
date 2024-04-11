package com.example.filetracking.inventory.data


import com.google.gson.annotations.SerializedName

class FetchRankByCategoryModelClass : ArrayList<FetchRankByCategoryModelClass.FetchRankByCategoryModelClassItem>(){
    data class FetchRankByCategoryModelClassItem(
        @SerializedName("name")
        val name: String
    )
}