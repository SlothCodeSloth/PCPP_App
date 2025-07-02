package com.example.pcpartpicker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ListOverviewFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListAdapter
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_list_overview, container, false)
        recyclerView = view.findViewById(R.id.listRecyclerView)
        fab = view.findViewById(R.id.addListFab)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ListAdapter { listEntity: ListEntity ->
            val intent = Intent(requireContext(), ComponentListActivity::class.java).apply {
                putExtra("list_name", listEntity.name)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        fab.setOnClickListener {
            showCreateListDialog()
        }
        loadLists()
        return view;
    }

    private fun showCreateListDialog() {
        val context = requireContext()
        val dialog = CreateListDialog(context) { listName ->
            val dao = (requireActivity().application as MyApplication).database.componentDao()
            lifecycleScope.launch {
                dao.insertList(ListEntity(name = listName))
                loadLists()
                Toast.makeText(context, "List \"$listName\" created", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadLists() {
        val dao = (requireActivity().application as MyApplication).database.componentDao()
        lifecycleScope.launch {
            val lists = dao.getAllListsWithComponents()
            adapter.submitList(lists.map {it.list})
        }
    }
}
