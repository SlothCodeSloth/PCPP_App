package com.example.pcpartpicker

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "components")
data class ComponentEntity(
    @PrimaryKey val url: String,
    val name: String,
    val price: String,
    val image: String?
)
