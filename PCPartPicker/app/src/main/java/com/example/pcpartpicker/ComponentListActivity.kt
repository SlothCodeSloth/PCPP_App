package com.example.pcpartpicker

import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class ComponentListActivity : AppCompatActivity() {

    private lateinit var listName: String
    private lateinit var adapter: ComponentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_list)

        val emptyText: TextView = findViewById(R.id.emptyTextView)
        val recyclerView: RecyclerView = findViewById(R.id.componentRecyclerView)
        val listTitle: TextView = findViewById(R.id.listTitleTextView)
        val bundleButton: FloatingActionButton = findViewById(R.id.bundleButton)
        listName = intent.getStringExtra("list_name") ?: ""
        listTitle.text = listName ?: "List"
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { part ->
                val intent = DetailActivity.newIntent(this, part, hideListButton = true)
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

                    SelectListDialog(this@ComponentListActivity, listNames, "Select a List") { selectedListName ->
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
                        updateTotalPrice()
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
                            image = it.image,
                            customPrice = it.customPrice
                        )
                    }
                    adapter.addComponents(parts)
                    emptyText.visibility = if (parts.isEmpty()) View.VISIBLE else View.GONE

                    updateTotalPrice()
                }
            }
        }
        else {
            Log.e("ComponentListActivity", "No list_name provided in intent.")
        }

        bundleButton.setOnClickListener {
            showCreateBundleDialog()
        }
    }

    private fun updateTotalPrice() {
        val totalPrice = findViewById<TextView>(R.id.priceTextView)
        val total = adapter.getAllComponents().sumOf { part ->
            val priceStr = part.customPrice?.takeIf { it.isNotBlank() } ?: part.price
            priceStr.replace(Regex("[^\\d.]"), "").toDoubleOrNull() ?: 0.0
        }
        val currencySymbol = SettingsDataManager.getCurrencySymbol(this)
        totalPrice.text = "Total: %s%.2f".format(currencySymbol, total)
    }

    private fun showCreateBundleDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_bundle, null)
        val bundleName: EditText = dialogView.findViewById(R.id.bundleNameTextView)
        val bundleVendor: EditText = dialogView.findViewById(R.id.bundleVendorTextView)
        val bundlePrice: EditText = dialogView.findViewById(R.id.bundlePriceTextView)
        val bundleUrl: EditText = dialogView.findViewById(R.id.bundleURLTextView)
        val bundleImage: EditText = dialogView.findViewById(R.id.bundleImageTextView)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        saveButton.isEnabled = false
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val nameFilled = bundleName.text.isNotEmpty()
                val priceFilled = bundlePrice.text.isNotEmpty()
                val urlFilled = bundleUrl.text.isNotEmpty()
                val vendorFilled = bundleVendor.text.isNotEmpty()
                val imageFilled = bundleImage.text.isNotEmpty()
                // Image is optional
                saveButton.isEnabled = nameFilled && priceFilled && urlFilled && vendorFilled
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        }

        bundleName.addTextChangedListener(textWatcher)
        bundleVendor.addTextChangedListener(textWatcher)
        bundlePrice.addTextChangedListener(textWatcher)
        bundleUrl.addTextChangedListener(textWatcher)
        bundleImage.addTextChangedListener(textWatcher)

        val dialog = AlertDialog.Builder(this, R.style.RoundCornerDialog)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val name = bundleName.text.toString()
            val vendor = bundleVendor.text.toString()
            val price = bundlePrice.text.toString()
            val url = bundleUrl.text.toString()
            val image = bundleImage.text.toString()

            lifecycleScope.launch {
                val dao = (application as MyApplication).database.bundleDao()
                val listDao = (application as MyApplication).database.componentDao()

                val allLists = listDao.getAllListsWithComponents()
                val matchedList = allLists.find { it.list.name == listName }
                if (matchedList == null) {
                    Toast.makeText(this@ComponentListActivity, "List not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val newBundle = BundleEntity(
                    name = name,
                    listId = matchedList.list.id,
                    vendor = vendor,
                    price = price,
                    url = url,
                    image = image
                )

                val bundleId = dao.insert(newBundle)

                showComponentSelectionDialog(matchedList.components) { selectedComponents ->
                    lifecycleScope.launch {
                        selectedComponents.forEach { component ->
                            dao.insertCrossRef(BundleComponentCrossRef(bundleId.toInt(), component.url))
                        }
                        Toast.makeText(this@ComponentListActivity, "Bundle Created", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

        val displayMetrics = DisplayMetrics()
        (this as? android.app.Activity)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val desiredWidth = (screenWidth * 0.9).toInt()
        dialog.window?.setLayout(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    private fun showComponentSelectionDialog(components: List<ComponentEntity>, onConfirm: (List<ComponentEntity>) -> Unit) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_select_components, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.componentsRecyclerView)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)

        val adapter = ComponentSelectAdapter(components)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            onConfirm(adapter.selectedItems.toList())
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}