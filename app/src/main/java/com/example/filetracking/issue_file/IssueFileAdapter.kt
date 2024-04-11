package com.example.filetracking.issue_file

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.filetracking.databinding.LayoutIssueFileBinding
import com.example.filetracking.databinding.RfidLayoutBinding
import com.example.filetracking.stock_take.data.Rfid
import com.example.filetracking.stock_take.data.RfidDetails


class IssueFileAdapter( private val mList: ArrayList<RfidDetails>) : RecyclerView.Adapter<IssueFileAdapter.ViewHOlder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = LayoutIssueFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)
    }

    override fun getItemCount(): Int = mList.size

    class ViewHOlder(private val itemBinding: LayoutIssueFileBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(items: RfidDetails) {
           // itemBinding.tvRfid.text = items.rfid
        }
    }


}