package com.example.pcpartpicker

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ComponentEntity::class,
        ListEntity::class,
        ListComponentCrossRef::class,
        BundleEntity:: class,
        BundleComponentCrossRef:: class
                ],
    version = 1
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun componentDao(): ComponentDao
    abstract fun bundleDao(): BundleDao
}