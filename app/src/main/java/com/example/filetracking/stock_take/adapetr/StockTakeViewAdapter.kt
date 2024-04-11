package com.example.filetracking.stock_take.adapetr

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.filetracking.databinding.StockTakeViewLayoutBinding
import com.example.filetracking.inventory.FetchCategoryByIDModelClass
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class StockTakeViewAdapter( private val mList: List<FetchCategoryByIDModelClass.FetchCategoryByIDItem>) : RecyclerView.Adapter<StockTakeViewAdapter.ViewHOlder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding =
            StockTakeViewLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)


    }

    override fun getItemCount(): Int = mList.size

    class ViewHOlder(private val itemBinding: StockTakeViewLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(items: FetchCategoryByIDModelClass.FetchCategoryByIDItem) {
            itemBinding.tvCategroy.text = items.category
            itemBinding.tvCdr.text = items.cdr
            itemBinding.tvRemark.text = items.remark
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
            if (items.isIssued == 1) {
                itemBinding.tvIssue.isVisible = true
                itemBinding.tvIssueTo.isVisible = true
                itemBinding.tvIssue.text = "File Issued"
                itemBinding.tvIssueTo.text = items.issuedTo

//                itemBinding.tvIssueHeading.isVisible = true
//                itemBinding.tvIsseudToHeading.isVisible = true
                itemBinding.llIssue.isVisible = true
                itemBinding.llIssueTo.isVisible = true
            } else {
                itemBinding.llIssue.isVisible = false
                itemBinding.tvIssue.isVisible = false
                itemBinding.tvIssueTo.isVisible = false
                itemBinding.llIssueTo.isVisible = false
//                itemBinding.tvIssueHeading.isVisible = false
//                itemBinding.tvIsseudToHeading.isVisible = false
            }


        }
    }


}