package com.example.pcpartpicker

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PartViewModel(private val api: PyPartPickerApi) : ViewModel() {

    // LiveData to hold the list of parts
    private val _parts = MutableLiveData<List<Component.Part>>()
    val parts: LiveData<List<Component.Part>> get() = _parts

    // LiveData to emit newly loaded components
    private val _newParts = MutableLiveData<List<Component.Part>>()
    val newParts: LiveData<List<Component.Part>> get() = _newParts

    // LiveData to handle loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData to handle errors
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    // Track query and current page
    private var currentQuery: String = ""
    private var currentPage: Int = 1
    private var totalPages: Int = 1
    private val pageSize: Int = 5

    // Start a new search
    fun startSearch(query: String) {
        if (query != currentQuery) {
            currentQuery = query
            currentPage = 1
            totalPages = 1
            _parts.value = emptyList()
            loadPage()
        }
    }

    // Load in new pages
    fun loadPage() {
        if (_isLoading.value == true || currentPage > totalPages) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = api.searchParts(
                    query = currentQuery,
                    limit = pageSize,
                    page = currentPage
                )

                totalPages = response.total_pages
                val newParts = response.results.map { part ->
                    Component.Part(
                        name = part.name,
                        url = part.url,
                        price = part.price?.toString() ?: "N/A",
                        image = part.image
                    )
                }

                val currentList = _parts.value?.toMutableList() ?: mutableListOf()
                currentList.addAll(newParts)
                _parts.value = currentList
                _newParts.value = newParts
                currentPage++
            }
            catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            }
            finally {
                _isLoading.value = false
            }
        }
    }

    // Fetch product details
    suspend fun fetchProduct(url: String): ProductResponse {
        return api.fetchProduct(url)
    }

    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}