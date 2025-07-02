package com.example.pcpartpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListAdapter(
    private val onClick: (ListEntity) -> Unit
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    private val lists = mutableListOf<ListEntity>()

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listName: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val list = lists[position]
        holder.listName.text = list.name
        holder.itemView.setOnClickListener { onClick(list) }
    }

    override fun getItemCount(): Int = lists.size

    fun submitList(newLists: List<ListEntity>) {
        lists.clear()
        lists.addAll(newLists)
        notifyDataSetChanged()
    }
}