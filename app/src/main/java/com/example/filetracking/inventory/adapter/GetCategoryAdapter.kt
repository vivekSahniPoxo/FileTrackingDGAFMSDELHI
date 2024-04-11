package com.example.filetracking.inventory.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.filetracking.R
import com.example.filetracking.inventory.CategoryModelClass

class GetCategoryAdapter(val context: Context, var mList: List<CategoryModelClass.CategoryModelClassItem>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val item: ItemHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.catogory_list, parent, false)
            item = ItemHolder(view)
            view?.tag = item
        } else {
            view = convertView
            item = view.tag as ItemHolder
        }

        item.category.text = mList[position].category
       // item.dividerLine.isVisible = false

        return view
    }

    override fun getItem(position: Int): Any {
        return mList[position]
    }

    override fun getCount(): Int {
        return mList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ItemHolder(itemView: View) {
        val category = itemView.findViewById(R.id.tv_rfid) as TextView
       // val dividerLine = itemView.findViewById(R.id.tv_divide_line) as View

    }
}
