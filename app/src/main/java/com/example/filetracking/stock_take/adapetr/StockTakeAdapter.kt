package com.example.filetracking.stock_take.adapetr

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.filetracking.databinding.RfidLayoutBinding
import com.example.filetracking.databinding.StockTakeLayoutBinding
import com.example.filetracking.inventory.FetchCategoryByIDModelClass
import com.example.filetracking.stock_take.data.Rfid


class StockTakeAdapter( private val mList: ArrayList<String>) : RecyclerView.Adapter<StockTakeAdapter.ViewHOlder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = RfidLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)
    }

    override fun getItemCount(): Int = mList.size

    class ViewHOlder(private val itemBinding: RfidLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(items: String) {
            itemBinding.tvRfid.text = items
        }
    }
}