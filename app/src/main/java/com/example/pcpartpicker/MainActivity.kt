package com.example.pcpartpicker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Part
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    private lateinit var api : PyPartPickerAPI
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Retrofit
        api = createRetrofitAPI()

        val searchButton = findViewById<Button>(R.id.searchButton)
        val searchText = findViewById<EditText>(R.id.searchText)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        searchButton.setOnClickListener {
            var searchTerm : String = searchText.text.toString()
            searchParts(searchTerm)
        }
    }

    // Initialize the Retrofit API
    private fun createRetrofitAPI(): PyPartPickerAPI {
        val retrofit = Retrofit.Builder()
            // API URL
            .baseUrl("https://ac7d-108-30-195-184.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(PyPartPickerAPI::class.java)
    }

    // Search for parts using the API
    private fun searchParts(query: String) {
        api.searchParts(query, 10, "us").enqueue(object: Callback<List<Component.Part>> {
            override fun onResponse(
                call: Call<List<Component.Part>>,
                response: Response<List<Component.Part>>
            ) {
                if (response.isSuccessful) {
                    val parts = response.body()
                    // Display results
                    if (parts != null) {
                        val adapter = ComponentAdapter(parts) {
                            part ->
                            val intent = Intent(this@MainActivity, DetailActivity::class.java).apply {
                                putExtra("product_name", part.name)
                                putExtra("product_price", part.price)
                                putExtra("product_image", part.image)
                                putExtra("product_url", part.url)
                            }
                            startActivity(intent)
                        }
                        recyclerView.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<List<Component.Part>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}
