package com.example.pcpartpicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailActivity : AppCompatActivity() {

    private val viewModel: PartViewModel by viewModels { PartViewModelFactory((application as MyApplication).api) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        // Acquire attributes from XML file
        val detailImage: ImageView = findViewById(R.id.detailImage)
        val detailName: TextView = findViewById(R.id.detailName)
        val detailPrice: TextView = findViewById(R.id.detailPrice)
        val detailLink: Button = findViewById(R.id.detailLink)
        val detailSpecs: TextView = findViewById(R.id.specs)

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

        // Fetch Product Specifications
        /*
        viewModel.fetchProduct(url ?: return).observe(this) { product ->
            val specs = product.specs.entries.joinToString("\n") { "${it.key}: ${it.value}"}
            detailSpecs.text = specs
        }
         */

        lifecycleScope.launch {
            try {
                val product = viewModel.fetchProduct(url ?: return@launch)
                val specs = product.specs.entries.joinToString ("\n"){ "${it.key}: ${it.value}" }
                detailSpecs.text = specs
            }
            catch (e: Exception) {
                Log.e("DetailActivity", "Error fetching product details: ${e.message}")
                detailSpecs.text = "Failed to load specifications."
            }
        }

        // TODO - Add to List Button

        //fetchProductSpecs(url, detailSpecs)
    }

    /*
    private fun fetchProductSpecs(url: String?,detailSpecs: TextView) {
        if (url == null) {
            return
        }
        Log.d("TAG", "Check 1")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://6dab-108-30-195-184.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d("TAG", "Check 2")
        val api = retrofit.create(PyPartPickerApi::class.java)
        Log.d("TAG", "Check 3")


        api.fetchProduct(url).enqueue(object : Callback<Component.Product> {
            override fun onResponse(
                call: Call<Component.Product>,
                response: Response<Component.Product>
            ) {
                if (response.isSuccessful) {
                    Log.d("TAG", "Check 4")
                    val product = response.body()
                    val specs = product?.specs?.entries?.joinToString("\n") { "${it.key}: ${it.value}"} ?: "No specs available"
                    detailSpecs.text = specs
                }
                else {
                    detailSpecs.text = "Failed to retrieve specs."
                }
            }

            override fun onFailure(call: Call<Component.Product>, t: Throwable) {
                detailSpecs.text = "Failed to retreive specs: ${t.message}"
            }
        })
    }
     */
}

    /*
    private fun fetchProductSpecs(url: String?,detailSpecs: TextView) {
        if (url == null) {
            return
        }
        Log.d("TAG", "Check 1")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://6dab-108-30-195-184.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d("TAG", "Check 2")
        val api = retrofit.create(PyPartPickerApi::class.java)
        Log.d("TAG", "Check 3")


        api.fetchProduct(url).enqueue(object : Callback<Component.Product> {
            override fun onResponse(
                call: Call<Component.Product>,
                response: Response<Component.Product>
            ) {
                if (response.isSuccessful) {
                    Log.d("TAG", "Check 4")
                    val product = response.body()
                    val specs = product?.specs?.entries?.joinToString("\n") { "${it.key}: ${it.value}"} ?: "No specs available"
                    detailSpecs.text = specs
                }
                else {
                    detailSpecs.text = "Failed to retrieve specs."
                }
            }

            override fun onFailure(call: Call<Component.Product>, t: Throwable) {
                detailSpecs.text = "Failed to retreive specs: ${t.message}"
            }
        })
    }
     */
//}