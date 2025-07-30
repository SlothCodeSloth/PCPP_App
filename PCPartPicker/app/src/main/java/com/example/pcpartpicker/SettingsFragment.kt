package com.example.pcpartpicker

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

class SettingsFragment : Fragment() {

    private val themeColors = listOf(
        R.drawable.theme_circle1,
        R.drawable.theme_circle2
    )

    private var selectedThemeIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val themeContainer = view.findViewById<LinearLayout>(R.id.themeContainer)
        val regionSpinner: Spinner = view.findViewById(R.id.regionSpinner)
        val regions = listOf("Australia", "Austria", "Belgium", "Canada", "Czech Republic",
            "Denmark", "Finland", "France", "Germany", "Hungary", "Ireland", "Italy", "Netherlands",
            "New Zealand", "Norway", "Portugal", "Romania", "Saudi Arabia", "Slovakia", "Spain",
            "Sweden","United States", "United Kingdom")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, regions)
        regionSpinner.adapter = adapter

        regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedRegion = regions[position]
                Toast.makeText(requireContext(), "Region set to $selectedRegion", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        themeColors.forEachIndexed { index, drawableRes ->
            val imageView = ImageView(requireContext()).apply {
                setImageResource(drawableRes)
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    marginEnd = 16
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(16, 16, 16, 16)

                setOnClickListener {
                    if (selectedThemeIndex != index) {
                        selectedThemeIndex = index
                        ThemeManager.saveThemeIndex(requireContext(), index)
                        highlightSelectedTheme(themeContainer, index)
                        Toast.makeText(requireContext(), "Theme ${index + 1} selected", Toast.LENGTH_SHORT).show()

                        // Recreate activity
                        ThemeManager.recreateActivity(requireActivity())
                    }
                    /*
                    selectedThemeIndex = index
                    saveTheme(index)
                    highlightSelectedTheme(themeContainer, index)
                    Toast.makeText(requireContext(), "Theme ${index + 1} selected", Toast.LENGTH_SHORT).show()
                     */
                }
            }
            themeContainer.addView(imageView)
        }
        selectedThemeIndex = ThemeManager.getSavedThemeIndex(requireContext())
        highlightSelectedTheme(themeContainer, selectedThemeIndex)
        return view;
    }

    private fun saveTheme(index: Int) {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .putInt("selected_theme", index)
            .apply()
    }

    private fun getSavedTheme(): Int {
        return PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt("selected_theme", 0)
    }

    private fun highlightSelectedTheme(container: LinearLayout, selectedTheme: Int) {
        for (i in 0 until container.childCount) {
            val imageView = container.getChildAt(i) as ImageView
            imageView.scaleX = if (i == selectedTheme) 1.15f else 1f
            imageView.scaleY = if (i == selectedTheme) 1.15f else 1f
            imageView.alpha = if (i == selectedTheme) 1f else 0.6f
        }
    }
}