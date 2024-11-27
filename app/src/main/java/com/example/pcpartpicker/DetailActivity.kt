package com.example.pcpartpicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        // Acquire attributes from XML file
        val detailImage: ImageView = findViewById(R.id.detailImage)
        val detailName: TextView = findViewById(R.id.detailName)
        val detailPrice: TextView = findViewById(R.id.detailPrice)
        val detailLink: Button = findViewById(R.id.detailLink)

        // Get Data from Main Activity (Intent)
        val name = intent.getStringExtra("product_name")
        val price = intent.getStringExtra("product_price")
        val image = intent.getStringExtra("product_image")
        val url = intent.getStringExtra("product_url")

        // Apply data to attributes
        detailName.text = name
        detailPrice.text = price
        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(detailImage)

        // Add clickable button
        detailLink.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        // TODO - Add to List Button
    }
}