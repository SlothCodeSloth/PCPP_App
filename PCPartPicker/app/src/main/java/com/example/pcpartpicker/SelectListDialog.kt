package com.example.pcpartpicker

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog

class SelectListDialog (
    context: Context,
    listNames: List<String>,
    private val onListSelected: (String) -> Unit
) {
    private val builder = AlertDialog.Builder(context)
    init {
        builder.setTitle("Select a List")
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, listNames)
        builder.setAdapter(adapter) { _, which ->
            onListSelected(listNames[which])
        }
        builder.setNegativeButton("Cancel", null)
    }

    fun show() {
        builder.show()
    }
}