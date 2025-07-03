package com.example.pcpartpicker

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class ComponentListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_list)

        val emptyText = findViewById<TextView>(R.id.emptyTextView)
        val recyclerView = findViewById<RecyclerView>(R.id.componentRecyclerView)
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
            }
        )

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