package com.example.pcpartpicker

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailActivity : AppCompatActivity() {

    private val viewModel: PartViewModel by viewModels { PartViewModelFactory((application as MyApplication).api) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail)

        // Acquire attributes from XML file
        val detailImage: ImageView = findViewById(R.id.detailImage)
        val detailName: TextView = findViewById(R.id.detailName)
        val detailPrice: TextView = findViewById(R.id.detailPrice)
        val detailLink: Button = findViewById(R.id.detailLink)
        //val detailSpecs: TextView = findViewById(R.id.specs)
        val specTable: TableLayout = findViewById(R.id.specTable)
        val listButton = findViewById<Button>(R.id.listButton)
        specTable.removeAllViews()

        // Get Data from Main Activity (Intent)
        val name = intent.getStringExtra("product_name")
        val price = intent.getStringExtra("product_price")
        val image = intent.getStringExtra("product_image")
        val url = intent.getStringExtra("product_url")

        // Get Currency Data
        var currency = SettingsDataManager.getCurrencySymbol(this)
        currency += price

        // Apply data to attributes
        detailName.text = name
        detailPrice.text = currency
        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(detailImage)

        // Add clickable button
        detailLink.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        lifecycleScope.launch {
            try {
                val product = viewModel.fetchProduct(url ?: return@launch)
                //val specs = product.specs.entries.joinToString ("\n"){ "${it.key}: ${it.value}" }
                //detailSpecs.text = specs
                for ((key ,value) in product.specs.entries) {
                    val row = TableRow(this@DetailActivity)

                    val keyText = TextView(this@DetailActivity)
                    keyText.text = key
                    keyText.setPadding(8, 8, 16, 8)
                    keyText.setTypeface(null, Typeface.BOLD)

                    val valueText = TextView(this@DetailActivity)
                    valueText.text = value
                    valueText.setPadding(8, 8, 8, 8)

                    row.addView(keyText)
                    row.addView(valueText)

                    specTable.addView(row)
                }
            }
            catch (e: Exception) {
                Log.e("DetailActivity", "Error fetching product details: ${e.message}")
                //detailSpecs.text = "Failed to load specifications."
            }
        }

        listButton.setOnClickListener {
            val dao = (application as MyApplication).database.componentDao()
            lifecycleScope.launch {
                val allLists = dao.getAllListsWithComponents()
                val listNames = allLists.map { it.list.name }

                if (listNames.isEmpty()) {
                    Toast.makeText(this@DetailActivity, "No lists found.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                SelectListDialog(this@DetailActivity, listNames) { selectedListName ->
                    val matchedList = allLists.find { it.list.name == selectedListName }
                    if (matchedList == null) {
                        Toast.makeText(this@DetailActivity, "Selected list not found.", Toast.LENGTH_SHORT).show()
                        return@SelectListDialog
                    }


                    lifecycleScope.launch {
                        val componentEntity = ComponentEntity(
                            url = url!!,
                            name = name ?: "Unknown",
                            price = price ?: "N/A",
                            image = image
                        )

                        dao.insertComponent(componentEntity)
                        dao.insertCrossRef(ListComponentCrossRef(matchedList.list.id, url))

                        Toast.makeText(this@DetailActivity, "Added to \"$selectedListName\"", Toast.LENGTH_SHORT).show()
                    }
                }.show()
            }
        }
    }

    companion object {
        fun newIntent(context: android.content.Context, part: Component.Part): Intent {
            return Intent(context, DetailActivity::class.java).apply {
                putExtra("product_name", part.name)
                putExtra("product_url", part.url)
                putExtra("product_price", part.price)
                putExtra("product_image", part.image)
            }
        }
    }
}