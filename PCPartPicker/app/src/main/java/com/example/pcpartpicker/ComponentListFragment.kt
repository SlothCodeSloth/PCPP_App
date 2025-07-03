package com.example.pcpartpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ComponentListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComponentAdapter
    private lateinit var listName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_component_list, container, false)
        recyclerView = view.findViewById(R.id.componentListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        /*
        adapter = ComponentAdapter(mutableListOf()) { part ->
            val intent = DetailActivity.newIntent(requireContext(), part)
            startActivity(intent)
        }
         */

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
                        Toast.makeText(requireContext(), "No Lists Found", Toast.LENGTH_SHORT).show()
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
                            price = selectedProduct.price ?: "N/A",
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
        loadListItems()
        return view
    }

    private fun loadListItems() {
        val dao = (requireActivity().application as MyApplication).database.componentDao()
        lifecycleScope.launch {
            val lists = dao.getAllListsWithComponents()
            val matched = lists.find { it.list.name == listName }
            val componentEntities = matched?.components ?: emptyList()

            matched?.components?.let { components: List<ComponentEntity> ->
                val parts = components.map {
                    Component.Part (
                        name = it.name,
                        url = it.url,
                        price = it.price,
                        image = it.image
                    )
                }
                adapter.addComponents(parts)
            }
        }
    }

    companion object {
        private const val ARG_LIST_NAME = "list_name"

        fun newInstance(listName: String): ComponentListFragment {
            val fragment = ComponentListFragment()
            val args = Bundle()
            args.putString(ARG_LIST_NAME, listName)
            fragment.arguments = args
            return fragment
        }
    }
}