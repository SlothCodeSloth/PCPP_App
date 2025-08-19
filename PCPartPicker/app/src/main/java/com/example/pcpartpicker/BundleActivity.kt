package com.example.pcpartpicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class BundleActivity : AppCompatActivity() {
    private val viewModel: PartViewModel by viewModels { PartViewModelFactory((application as MyApplication).api) }
    private lateinit var componentAdapter: ComponentAdapter
    private val BUNDLE_ACTIVITY_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_bundle)

        val listName: TextView = findViewById(R.id.listTitleTextView)
        val bundleName: TextView = findViewById(R.id.bundleNameTextView)
        val bundlePrice: TextView = findViewById(R.id.bundlePriceTextView)
        val bundleUrl: Button = findViewById(R.id.bundleURLButton)
        val recyclerView: RecyclerView = findViewById(R.id.componentRecyclerView)
        val addFab: FloatingActionButton = findViewById(R.id.addComponentFab)
        val settingsFab: FloatingActionButton = findViewById(R.id.bundleSettingsFab)

        val bundleId = intent.getIntExtra("bundle_id", -1)
        val name = intent.getStringExtra("bundle_name")
        val price = intent.getStringExtra("bundle_price")?: "0,0"
        val url = intent.getStringExtra("bundle_url")
        val listId = intent.getIntExtra("list_id", -1)
        listName.text = intent.getStringExtra("list_name") ?: "List Name"
        recyclerView.layoutManager = LinearLayoutManager(this)

        componentAdapter = ComponentAdapter(
            mutableListOf(),
            onItemClick = { item ->
                when (item) {
                    is ListItem.ComponentItem -> {
                        val intent = DetailActivity.newIntent(this, item.component, hideListButton = true)
                        startActivity(intent)
                    }

                    is ListItem.BundleItem -> {
                    }
                }
            },

            onAddClick = { selectedComponent ->

            }
        )

        recyclerView.adapter = componentAdapter
        bundleName.text = name
        bundlePrice.text = "Total: " + SettingsDataManager.formatPrice(this, price)

        bundleUrl.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        if (bundleId != -1) {
            val dao = (application as MyApplication).database.bundleDao()
            lifecycleScope.launch {
                val bundleWithComponents = dao.getBundleWithComponents(bundleId)
                bundleWithComponents?.components?.let { components ->
                    components.forEach { componentEntity ->
                        componentAdapter.addComponents(componentEntity)
                    }
                }
            }
        }

        addFab.setOnClickListener {
            if (bundleId != -1) {
                showComponentSelectionDialog(bundleId) { selectedComponents ->
                    lifecycleScope.launch {
                        val bundleDao = (application as MyApplication).database.bundleDao()
                        val componentDao = (application as MyApplication).database.componentDao()
                        selectedComponents.forEach { component ->
                            // Add to bundle
                            val crossRef = BundleComponentCrossRef(bundleId, component.url)
                            bundleDao.insertCrossRef(crossRef)

                            // Remove from List
                            componentDao.deleteCrossRef(listId, component.url)

                            val listUsageCount = componentDao.getListCountForComponent(component.url)
                            val bundleUsageCount = bundleDao.getBundleCountForComponent(component.url)

                            if (listUsageCount == 0 && bundleUsageCount == 0) {
                                componentDao.deleteComponent(component.url)
                            }
                        }


                        runOnUiThread {
                            selectedComponents.forEach { component ->
                                componentAdapter.addComponents(component)
                            }
                            Toast.makeText(this@BundleActivity, "Component(s) moved to Bundle", Toast.LENGTH_SHORT).show()
                            finish()
                        }


                    }
                }
            }
        }

        settingsFab.setOnClickListener {
            if (bundleId != -1) {
                lifecycleScope.launch {
                    val bundleDao = (application as MyApplication).database.bundleDao()
                    val currentBundle = bundleDao.getBundle(bundleId)
                    currentBundle?.let { bundle ->
                        showEditBundleDialog(bundle) { updatedBundle ->
                            lifecycleScope.launch {
                                bundleDao.update(updatedBundle)
                                runOnUiThread {
                                    bundleName.text = updatedBundle.name
                                    bundlePrice.text = "Total: " + SettingsDataManager.formatPrice(this@BundleActivity, updatedBundle.price)
                                    bundleUrl.tag = updatedBundle.url
                                    bundleUrl.text = updatedBundle.vendor
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newIntent(context: android.content.Context, bundle: BundleEntity): Intent {
            return Intent(context, BundleActivity::class.java).apply {
                putExtra("bundle_name", bundle.name)
                putExtra("bundle_url", bundle.url)
                putExtra("bundle_price", bundle.price)
                putExtra("bundle_id", bundle.bundleId)
            }
        }
    }

    private fun showComponentSelectionDialog(bundleId: Int, onConfirm: (List<ComponentEntity>) -> Unit) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_select_components, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.componentsRecyclerView)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)

        lifecycleScope.launch {
            val componentDao = (application as MyApplication).database.componentDao()
            val bundleDao = (application as MyApplication).database.bundleDao()

            val allComponents = componentDao.getAllComponents()
            val bundleWithComponent = bundleDao.getBundleWithComponents(bundleId)
            val existingUrls = bundleWithComponent?.components?.map { it.url } ?: emptyList()
            val componentsToAdd = allComponents.filter { it.url !in existingUrls }

            runOnUiThread {
                val adapter = ComponentSelectAdapter(componentsToAdd)
                recyclerView.layoutManager = LinearLayoutManager(this@BundleActivity)
                recyclerView.adapter = adapter
                val dialog = AlertDialog.Builder(this@BundleActivity).setView(dialogView).create()

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
    }

    private fun showEditBundleDialog(currentBundle: BundleEntity, onSave: (BundleEntity) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_bundle, null)
        val bundleName: EditText = dialogView.findViewById(R.id.bundleNameTextView)
        val bundleVendor: EditText = dialogView.findViewById(R.id.bundleVendorTextView)
        val bundlePrice: EditText = dialogView.findViewById(R.id.bundlePriceTextView)
        val bundleUrl: EditText = dialogView.findViewById(R.id.bundleURLTextView)
        val bundleImage: EditText = dialogView.findViewById(R.id.bundleImageTextView)
        val saveButton: Button = dialogView.findViewById(R.id.saveButton)
        val cancelButton: Button = dialogView.findViewById(R.id.cancelButton)

        bundleName.setText(currentBundle.name)
        bundlePrice.setText(currentBundle.price)
        bundleUrl.setText(currentBundle.url)
        bundleImage.setText(currentBundle.image)
        bundleVendor.setText(currentBundle.vendor)

        val dialog = AlertDialog.Builder(this, R.style.RoundCornerDialog)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val newBundle = currentBundle.copy(
                name = bundleName.text.toString(),
                price = bundlePrice.text.toString(),
                url = bundleUrl.text.toString(),
                image = bundleImage.text.toString(),
                vendor = bundleVendor.text.toString()
            )
            onSave(newBundle)
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}