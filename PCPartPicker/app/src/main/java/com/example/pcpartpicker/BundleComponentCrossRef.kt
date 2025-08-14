package com.example.pcpartpicker

import androidx.room.Entity

@Entity(primaryKeys = ["bundleId", "url"])
data class BundleComponentCrossRef(
    val bundleId: Int,
    val url: String
)
