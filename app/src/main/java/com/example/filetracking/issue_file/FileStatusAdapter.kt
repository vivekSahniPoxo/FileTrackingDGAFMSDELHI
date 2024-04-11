package com.example.filetracking.issue_file

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.filetracking.databinding.LayoutIssueFileBinding
import com.example.filetracking.inventory.FetchCategoryByIDModelClass

import com.example.filetracking.utils.ItemSwipeListener
import java.text.SimpleDateFormat
import java.util.*


class FileStatusAdapter( val swipeListener: ItemSwipeListener, private val mList: ArrayList<FetchCategoryByIDModelClass.FetchCategoryByIDItem>) : RecyclerView.Adapter<FileStatusAdapter.ViewHOlder>() {

    var itemList: MutableList<FetchCategoryByIDModelClass.FetchCategoryByIDItem> = mutableListOf()
    var tempItemList: MutableList<FetchCategoryByIDModelClass.FetchCategoryByIDItem> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = LayoutIssueFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }


    fun removeItemFromAdapter(position: Int) {
        if (position in 0 until mList.size) {
            // Remove the item from the dataset
            mList.removeAt(position)

            // Notify the adapter about the removal
            notifyItemRemoved(position)
        }
    }

    fun removeItem(position: Int) {
        if (position in 0 until itemList.size) {
            // Remove the item from the itemList
            val deletedItem = itemList.removeAt(position)
            tempItemList.remove(deletedItem)

            Log.d("afetrRemove", itemList.size.toString())

            // Notify the adapter about the removal
            notifyItemRemoved(position)

            // Update tempItemList
            tempItemList = ArrayList(itemList)
            mList.removeAt(position)


        }
    }



    fun getCurrentItemCount(): Int {
        return itemList.size
        notifyDataSetChanged()
    }


    fun getAllItems(): List<FetchCategoryByIDModelClass.FetchCategoryByIDItem> {
        return mList.toList()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)



    }

    override fun getItemCount(): Int = mList.size

    class ViewHOlder(private val itemBinding: LayoutIssueFileBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(items: FetchCategoryByIDModelClass.FetchCategoryByIDItem) {
            itemBinding.tvCategroy.text = items.category
            itemBinding.tvCdr.text = items.cdr
            //itemBinding.tvRemark.text = items.remark
            itemBinding.tvRank.text = items.rank
            val inputDateString = items.createdAt
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS", Locale.US)
            val outputFormat = SimpleDateFormat("MMM d h:mm a", Locale.US)


            try {
                val date = inputFormat.parse(inputDateString)
                val formattedDate = outputFormat.format(date)
                itemBinding.tvCreated.text = formattedDate.toString()

                println("Formatted Date: $formattedDate")
            } catch (e: Exception) {
                println("Error parsing date: ${e.message}")
            }




        }






    }







}