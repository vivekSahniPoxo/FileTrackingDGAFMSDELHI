package com.example.filetracking.inventory


import com.google.gson.annotations.SerializedName

class CategoryModelClass : ArrayList<CategoryModelClass.CategoryModelClassItem>(){
    data class CategoryModelClassItem(
        @SerializedName("category")
        val category: String,
        @SerializedName("categoryid")
        val categoryid: Int
    )
}