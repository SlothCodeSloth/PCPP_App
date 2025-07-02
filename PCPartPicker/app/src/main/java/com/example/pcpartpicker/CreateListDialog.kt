package com.example.pcpartpicker

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class CreateListDialog (
    private val context: Context,
    private val onListCreated: (String) -> Unit
) {
    fun show() {
        val input = EditText(context)
        AlertDialog.Builder(context)
            .setTitle("Create New List")
            .setMessage("Enter List Name")
            .setView(input)
            .setPositiveButton("Create") {_, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    onListCreated(name)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}