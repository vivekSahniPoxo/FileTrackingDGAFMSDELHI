package com.example.filetracking.inventory.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.example.filetracking.R
import com.example.filetracking.databinding.StockTakeLayoutBinding
import com.example.filetracking.inventory.BackgoundChange
import com.example.filetracking.inventory.FetchCategoryByIDModelClass
import com.example.filetracking.inventory.data.InventoryDataClass
import com.example.filetracking.inventory.data.RfidItem
import com.example.filetracking.stock_take.data.StockData
import kotlinx.coroutines.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class FetchCategoryByID(private val mList: MutableList<FetchCategoryByIDModelClass.FetchCategoryByIDItem>) :
    RecyclerView.Adapter<FetchCategoryByID.ViewHOlder>() {

    val rfidTagsSet = HashSet<String>()
    private var foundValue: String? = null
    val temList = arrayListOf<String>()
    var totalCountSize = 0
    var foundList = arrayListOf<String>()
    val highlightedPositions = mutableSetOf<String>()

    private val pageSize = 100
    private val sortedChunks = mutableListOf<List<FetchCategoryByIDModelClass.FetchCategoryByIDItem>>()
    private var currentPage = 0

//    init {
//        sortAndPaginate()
//    }




   var AllRfidList:ArrayList<RfidItem>?=null
    private val handler = Handler(Looper.getMainLooper())

    private var coloredItemCount = 0 // Keep track of the number of items with color change

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = StockTakeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)


    }





    override fun getItemCount(): Int = mList.size

    inner class ViewHOlder(private val itemBinding: StockTakeLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        private var lastMatchedTag: String? = null

        fun bind(items: FetchCategoryByIDModelClass.FetchCategoryByIDItem) {
            itemBinding.tvCategroy.text = items.category
            itemBinding.tvCdr.text = items.cdr
            itemBinding.tvRemark.text = items.remark
            itemBinding.tvRank.text = items.rank
            itemBinding.tvStatus.text = items.status?:"Not Found"

            val inputDateString = items.createdAt
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS", Locale.US)
            val outputFormat = SimpleDateFormat("MMM d h:mm a", Locale.US)

            foundList.add(items.rfidNo.toUpperCase())

            try {
                val date = inputFormat.parse(inputDateString)
                val formattedDate = outputFormat.format(date)
                itemBinding.tvCreated.text = formattedDate.toString()

                println("Formatted Date: $formattedDate")
            } catch (e: Exception) {
                println("Error parsing date: ${e.message}")
            }

            if (items.status=="Found"){
                itemBinding.ll.setBackgroundColor(
                            ContextCompat.getColor(
                                itemView.context,
                                R.color.green2
                            )
                        )
            } else{
                itemBinding.ll.setBackgroundColor(Color.TRANSPARENT)
            }



            if (items.isIssued == 1) {
                itemBinding.tvIssue.isVisible = true
                itemBinding.tvIssueTo.isVisible = true
                itemBinding.tvIssue.text = "File Issued"
                itemBinding.tvIssueTo.text = items.issuedTo
                itemBinding.tvIssueHeading.isVisible = true
                itemBinding.tvIsseudToHeading.isVisible = true
                itemBinding.llIssue.isVisible = true
                itemBinding.llIssueTo.isVisible = true
            } else {
                itemBinding.llIssue.isVisible = false
                itemBinding.tvIssue.isVisible = false
                itemBinding.tvIssueTo.isVisible = false
                itemBinding.llIssueTo.isVisible = false
                itemBinding.tvIssueHeading.isVisible = false
                itemBinding.tvIsseudToHeading.isVisible = false
            }
        }




    }






    @SuppressLint("LongLogTag")
    fun changeRfidTag(tags: List<String>) {
        GlobalScope.launch(Dispatchers.Default) {
            val updatedPositions = mutableListOf<Int>()

            tags.forEach { tag ->
                val index = mList.indexOfFirst { it.rfidNo.equals(tag, ignoreCase = true) }
                if (index != -1 && rfidTagsSet.add(tag.toUpperCase())) {
                    updatedPositions.add(index)
                }
            }
            val starTime = System.currentTimeMillis()
            Log.d("Process:Sorting StartTime",starTime.toString())
            // Sort the entire list based on the condition
            mList.sortBy { item ->
                if (rfidTagsSet.contains(item.rfidNo.toUpperCase())) {
                    0 // Matched items come last

                } else {
                    1 // Non-matched items come first

                }

            }
            Log.d("Process:Sorting EndTime",System.currentTimeMillis().toString())
            Log.d("Process:Sorting StartTime", (System.currentTimeMillis()-starTime).toString())

            if (updatedPositions.isNotEmpty()) {
                // Batch the UI updates
                val batchSize = 50 // Choose an appropriate batch size
                val batches = updatedPositions.chunked(batchSize)

                // Update the UI on the main thread in batches
                batches.forEach { batch ->
                    launch(Dispatchers.Main) {
                        batch.forEach { position ->
                            notifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }







    fun clearAllData() {
        rfidTagsSet.clear()
        temList.clear()
        coloredItemCount = 0
        mList.clear()
        notifyDataSetChanged()
    }

    fun refreshAdapter() {
//        totalCountSize = temList.distinct().size
        handler.post {
            notifyDataSetChanged()
        }
    }

    fun updateStatus(rfidNo: String, newStatus: String) {
        GlobalScope.launch(Dispatchers.Main) {
            // Update the status in the adapter's data
            val foundItem = mList.find { it.rfidNo.equals(rfidNo, ignoreCase = true) }
            foundItem?.status = newStatus

            // Find the position of the item in the adapter
            val position = mList.indexOf(foundItem)

            // Notify the adapter about the specific item change
            if (position != -1) {
                notifyItemChanged(position)
            }
        }
    }


    fun moveFoundItemsToTop(loader: ProgressBar, recyclerView: RecyclerView) {
        loader.isVisible = true

        GlobalScope.launch(Dispatchers.Default) {
            // Filter found items
            //val foundItems = mList.filter { highlightedPositions.contains(it.rfidNo.toUpperCase()) }


            runOnUiThread(Runnable {
                mList.sortByDescending { it.status == "Found" }
                notifyDataSetChanged()
                loader.isVisible = false
            })

//            if (foundItems.isNotEmpty()) {
//                withContext(Dispatchers.Main) {
//                    // Remove found items from their current positions
//                    foundItems.forEach { item ->
//                        val index = mList.indexOf(item)
//                        mList.remove(item)
//                        notifyItemRemoved(index)
//                    }
//
//                    // Add found items to the top of the list
//                    mList.addAll(0, foundItems)
//
//                    // Notify the adapter about the changes
//                    notifyItemRangeInserted(0, foundItems.size)
//                    loader.isVisible = false
//                    recyclerView.smoothScrollToPosition(0)
//                }
//            }
        }
    }















}




