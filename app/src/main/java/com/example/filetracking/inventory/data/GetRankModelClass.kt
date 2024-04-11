package com.example.filetracking.inventory.data


import com.google.gson.annotations.SerializedName

class GetRankModelClass : ArrayList<GetRankModelClass.GetRankModelClassItem>(){
    data class GetRankModelClassItem(
        @SerializedName("name")
        val name: String
    )
}