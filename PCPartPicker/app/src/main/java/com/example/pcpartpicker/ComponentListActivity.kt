package com.example.pcpartpicker

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ComponentListActivity : AppCompatActivity() {

    private lateinit var listName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_list)

        val emptyText = findViewById<TextView>(R.id.emptyTextView)
        val recyclerView = findViewById<RecyclerView>(R.id.componentRecyclerView)
        listName = intent.getStringExtra("list_name") ?: ""
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { part ->
                val intent = DetailActivity.newIntent(this, part)
                startActivity(intent)
            },
            onAddClick = { selectedProduct ->
                val dao = (application as MyApplication).database.componentDao()
                lifecycleScope.launch {
                    val allLists = dao.getAllListsWithComponents()
                    val listNames = allLists.map { it.list.name }
                    if (listNames.isEmpty()) {
                        Toast.makeText(this@ComponentListActivity, "No Lists Found", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    SelectListDialog(this@ComponentListActivity, listNames) { selectedListName ->
                        val matchedList = allLists.find { it.list.name == selectedListName }
                        if (matchedList == null) {
                            Toast.makeText(this@ComponentListActivity, "List Not Found", Toast.LENGTH_SHORT).show()
                            return@SelectListDialog
                        }

                        val componentEntity = ComponentEntity(
                            url = selectedProduct.url,
                            name = selectedProduct.name ?: "Unknown",
                            price = selectedProduct.price ?: "N/A",
                            image = selectedProduct.image
                        )

                        lifecycleScope.launch {
                            dao.insertComponent(componentEntity)
                            dao.insertCrossRef(ListComponentCrossRef(matchedList.list.id, selectedProduct.url))
                            Toast.makeText(this@ComponentListActivity, "Added to \"$selectedListName\"", Toast.LENGTH_SHORT).show()
                        }
                    }.show()
                }
            },
            showButton = false
        )

        // New
        val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val part = adapter.getComponentAt(position)

                val dao = (application as MyApplication).database.componentDao()
                lifecycleScope.launch {
                    val allLists = dao.getAllListsWithComponents()
                    val matchedList = allLists.find { it.list.name == listName }

                    matchedList?.let {
                        // Delete the cross reference
                        dao.deleteCrossRef(matchedList.list.id, part.url)

                        // Delete the component if it is not in any other list
                        val usageCount = dao.getListCountForComponent(part.url)
                        if (usageCount == 0) {
                            dao.deleteComponent(part.url)
                        }

                        adapter.removeComponentAt(position)
                        Toast.makeText(this@ComponentListActivity, "Component removed from list", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter

        val listName = intent.getStringExtra("list_name")
        title = listName ?: "List"

        if (listName != null) {
            val dao = (application as MyApplication).database.componentDao()
            lifecycleScope.launch {
                val allLists = dao.getAllListsWithComponents()
                val matchedList = allLists.find { it.list.name == listName}
                matchedList?.components?.let { components ->
                    val parts = components.map {
                        Component.Part(
                            name = it.name,
                            url = it.url,
                            price = it.price,
                            image = it.image
                        )
                    }
                    adapter.addComponents(parts)
                    emptyText.visibility = if (parts.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
        else {
            Log.e("ComponentListActivity", "No list_name provided in intent.")
        }
    }
}