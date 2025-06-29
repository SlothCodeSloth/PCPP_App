package com.example.pcpartpicker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    // Oconomowoc
    private val viewModel: PartViewModel by viewModels { PartViewModelFactory((application as MyApplication).api) }
    private lateinit var api : PyPartPickerApi
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComponentAdapter
    private var isLoading = false
    private val PAGE_SIZE = 5
    private var currentPage = 1
    private lateinit var currentSearchTerm: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Retrofit
        // api = createRetrofitAPI()

        val searchButton = findViewById<Button>(R.id.searchButton)
        val searchText = findViewById<EditText>(R.id.searchText)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = ComponentAdapter(mutableListOf()) { part ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("product_name", part.name)
                putExtra("product_url", part.url)
                putExtra("product_price", part.price)
                putExtra("product_image", part.image)
            }
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up Search Button
        searchButton.setOnClickListener {
            val query = searchText.text.toString()
            if (query.isNotEmpty()) {
                adapter.clearComponents()
                viewModel.startSearch(query)
            }
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (viewModel.isLoading.value != true &&
                    layoutManager.findLastVisibleItemPosition() >= adapter.itemCount - 1) {
                    viewModel.loadPage()
                }
            }
        })

        // Observe LiveData for Parts
        viewModel.newParts.observe(this, Observer { newItems ->
            adapter.addComponents(newItems)
        })

        // Observe LiveData for loading state
        viewModel.isLoading.observe(this, Observer { isLoading ->
            // Display loading indicator
            if (isLoading) {

            }
            else {

            }
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            // Display error message
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        })

        /*
        //recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ComponentAdapter(mutableListOf()) { part ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("product_name", part.name)
                putExtra("product_url", part.url)
                putExtra("product_price", part.price)
                putExtra("product_image", part.image)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchButton.setOnClickListener {
            //var searchTerm : String = searchText.text.toString()
            //currentSearchTerm = searchTerm
            currentSearchTerm = searchText.text.toString()
            currentPage = 1
            adapter.addComponents(emptyList())
            loadMoreData(adapter)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Load more components
                if (!isLoading && lastVisibleItem >= totalItemCount - 2) {
                    loadMoreData(adapter)
                }
            }
        })
    }

    // Initialize the Retrofit API
    private fun createRetrofitAPI(): PyPartPickerApi {
        val retrofit = Retrofit.Builder()
            // API URL
            .baseUrl("https://6dab-108-30-195-184.ngrok-free.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(PyPartPickerApi::class.java)
    }

    private fun loadMoreData(adapter: ComponentAdapter) {
        isLoading = true
        api.searchParts(currentSearchTerm, currentPage * PAGE_SIZE, "us")
            .enqueue(object : Callback<List<Component.Part>> {
                override fun onResponse(
                    call: Call<List<Component.Part>>,
                    response: Response<List<Component.Part>>
                ) {
                    if (response.isSuccessful) {
                        val newParts = response.body() ?: emptyList()
                        adapter.addComponents(newParts)
                        currentPage++
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<List<Component.Part>>, t: Throwable) {
                    t.printStackTrace()
                    isLoading = false
                }
            })

         */
    }
}
