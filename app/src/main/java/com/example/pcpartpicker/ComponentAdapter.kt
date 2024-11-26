package com.example.pcpartpicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ComponentAdapter (private val products: List<Component.Part>):
    RecyclerView.Adapter<ComponentAdapter.ProductViewHolder>() {
            class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val productImage: ImageView = itemView.findViewById(R.id.componentImage)
                val productName: TextView = itemView.findViewById(R.id.componentName)
                val productPrice: TextView = itemView.findViewById(R.id.componentPrice)
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.component, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productName.text = product.name
        holder.productPrice.text = product.price
        Glide.with(holder.itemView.context)
            .load(product.image)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.productImage)
    }

    override fun getItemCount(): Int = products.size
}
