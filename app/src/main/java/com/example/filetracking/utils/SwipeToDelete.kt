package com.example.filetracking.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.filetracking.R
import com.example.filetracking.issue_file.FileStatusAdapter

class SwipeToDeleteCallback(private val adapter: FileStatusAdapter) : ItemTouchHelper.Callback() {
    var listSize: String = ""

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // No move action for swipe-to-delete
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Get the adapter position of the swiped item
        val position = viewHolder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            // Remove the item from the adapter
            val deletedItem = adapter.removeItem(position)
            adapter.notifyItemRemoved(position)
            listSize = adapter.getCurrentItemCount().toString()

            // Notify the activity about the swiped item position
            adapter.swipeListener.onItemSwiped(position, adapter.itemList)

            // Change background color to red
            viewHolder.itemView.setBackgroundColor(
                ContextCompat.getColor(viewHolder.itemView.context, R.color.red)
            )

            // Delay the removal of the item from the UI to allow time for the swipe animation
            // Handler(Looper.getMainLooper()).postDelayed({
            // Notify the adapter of the item removal after the swipe animation
            adapter.notifyItemRemoved(position)
            listSize = adapter.getCurrentItemCount().toString()

            // Reset the background color to its original state
            viewHolder.itemView.setBackgroundColor(
                ContextCompat.getColor(viewHolder.itemView.context, android.R.color.transparent)
            )

            //}, 0) // Change the delay time according to your preference
        } else{
            Log.d("ThreadCheck", "onSwiped executed on thread: ${Thread.currentThread().name}")
        }
    }

}


