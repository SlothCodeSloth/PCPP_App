package com.example.pcpartpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainSearchFragment : Fragment() {
    private val viewModel: PartViewModel by activityViewModels {
        PartViewModelFactory((requireActivity().application as MyApplication).api)
    }

    private lateinit var adapter: ComponentAdapter

    private val productTypes = mapOf(
        "All" to "null",
        "Keyboard" to "keyboard",
        "Speaker" to "speaker",
        "Monitor" to "monitor",
        "Thermal Paste" to "thermal-paste",
        "Video Card" to "video-card",
        "Case Fan" to "case-fan",
        "OS" to "os",
        "CPU Cooler" to "cpu-cooler",
        "Fan Controller" to "fan-controller",
        "UPS" to "ups",
        "Wired Network Card" to "wired-network-card",
        "Memory" to "memory",
        "Headphones" to "headphones",
        "Sound Card" to "sound-card",
        "Internal Hard Drive" to "internal-hard-drive",
        "Mouse" to "mouse",
        "Wireless Network Card" to "wireless-network-card",
        "Power Supply" to "power-supply",
        "Webcam" to "Webcam",
        "Motherboard" to "motherboard",
        "External Hard Drive" to "external-hard-drive",
        "Optical Drive" to "optical-drive",
        "Case" to "case",
        "CPU" to "cpu"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_search, container, false)
        val searchText = view.findViewById<EditText>(R.id.searchText)
        val searchButton = view.findViewById<Button>(R.id.searchButton)
        val filterButton = view.findViewById<Button>(R.id.filterButton)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { part ->
                val intent = DetailActivity.newIntent(requireContext(), part)
                startActivity(intent)
            },
            onAddClick = { selectedProduct ->
                val dao = (requireActivity().application as MyApplication).database.componentDao()
                viewLifecycleOwner.lifecycleScope.launch {
                    val allLists = dao.getAllListsWithComponents()
                    val listNames = allLists.map { it.list.name }
                    if (listNames.isEmpty()) {
                        Toast.makeText(requireContext(), "No Lists Found.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    SelectListDialog(requireContext(), listNames) { selectedListName ->
                        val matchedList = allLists.find { it.list.name == selectedListName }
                        if (matchedList == null) {
                            Toast.makeText(requireContext(), "List Not Found", Toast.LENGTH_SHORT).show()
                            return@SelectListDialog
                        }

                        val componentEntity = ComponentEntity(
                            url = selectedProduct.url,
                            name = selectedProduct.name ?: "Unknown",
                            price = selectedProduct.price ?:"N/A?",
                            image = selectedProduct.image
                        )

                        viewLifecycleOwner.lifecycleScope.launch {
                            dao.insertComponent(componentEntity)
                            dao.insertCrossRef(ListComponentCrossRef(matchedList.list.id, selectedProduct.url))
                            Toast.makeText(requireContext(), "Added to \"$selectedListName\"", Toast.LENGTH_SHORT).show()
                        }
                    }.show()
                }
            }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchButton.setOnClickListener {
            val query = searchText.text.toString()
            if (query.isNotEmpty()) {
                adapter.clearComponents()
                viewModel.startSearch(query, null, requireContext())
            }
        }

        filterButton.setOnClickListener {
            showFilterDialog()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                if (viewModel.isLoading.value != true &&
                    layoutManager.findLastVisibleItemPosition() >= adapter.itemCount - 1) {
                    viewModel.loadPage(requireContext())
                }
            }
        })

        viewModel.newParts.observe(viewLifecycleOwner, Observer { newItems ->
            adapter.addComponents(newItems)
        })

        return view
    }

    private fun showFilterDialog() {
        val filterNames = productTypes.keys.toTypedArray()
        val query = requireView().findViewById<EditText>(R.id.searchText)?.text.toString()
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select a Product Category")
            .setItems(filterNames) { dialog, which ->
                val selectedFilterName = filterNames[which]
                val selectedProductType = productTypes[selectedFilterName]

                if (query.isNotEmpty()) {
                    adapter.clearComponents()
                    viewModel.startSearch(query, selectedProductType, requireContext())
                }
                Toast.makeText(requireContext(), "Filter set to: $selectedFilterName", Toast.LENGTH_SHORT).show()
            }
        builder.create().show()
    }
}

/*
package com.example.pcpartpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainSearchFragment : Fragment() {
    private val viewModel: PartViewModel by activityViewModels {
        PartViewModelFactory((requireActivity().application as MyApplication).api)
    }

    private lateinit var adapter: ComponentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_search, container, false)
        val searchText = view.findViewById<EditText>(R.id.searchText)
        val searchButton = view.findViewById<Button>(R.id.searchButton)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = ComponentAdapter(mutableListOf()) { part ->
            val intent = DetailActivity.newIntent(requireContext(), part)
            startActivity(intent)
        },

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

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

        viewModel.newParts.observe(viewLifecycleOwner, Observer { newItems ->
            adapter.addComponents(newItems)
        })

        return view
    }
 */