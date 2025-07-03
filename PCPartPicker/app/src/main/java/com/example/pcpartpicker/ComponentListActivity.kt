package com.example.pcpartpicker

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.TextView
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

        val adapter = ComponentAdapter(mutableListOf()) { part ->
            val intent = DetailActivity.newIntent(this, part)
            startActivity(intent)
        }

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