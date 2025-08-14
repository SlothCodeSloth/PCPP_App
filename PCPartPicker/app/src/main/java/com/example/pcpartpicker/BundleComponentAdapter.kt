package com.example.pcpartpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BundleComponentAdapter(
    private val components: List<ComponentEntity>
) : RecyclerView.Adapter<BundleComponentAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.componentName)
        val imageView: ImageView = itemView.findViewById(R.id.componentImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val component = components[position]
        holder.nameText.text = component.name
        Glide.with(holder.itemView.context)
            .load(component.image)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imageView)

        holder.itemView.findViewById<TextView>(R.id.componentPrice)?.visibility = View.GONE
        holder.itemView.findViewById<Button>(R.id.componentButton)?.visibility = View.GONE
        holder.itemView.findViewById<TextView>(R.id.componentPriceAlt)?.visibility = View.GONE
    }

    override fun getItemCount(): Int = components.size
}