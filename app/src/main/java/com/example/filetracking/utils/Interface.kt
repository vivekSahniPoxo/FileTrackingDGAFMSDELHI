package com.example.filetracking.utils

import com.example.filetracking.inventory.FetchCategoryByIDModelClass



interface ItemSwipeListener {
    fun onItemSwiped(position: Int,updatedList: List<FetchCategoryByIDModelClass.FetchCategoryByIDItem>)
}