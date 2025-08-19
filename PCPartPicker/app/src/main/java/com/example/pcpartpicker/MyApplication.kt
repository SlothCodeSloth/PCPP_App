package com.example.pcpartpicker

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.room.Room

class MyApplication : Application() {
    val api: PyPartPickerApi by lazy {
        Retrofit.Builder()
            .baseUrl("") // Insert API Url here
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PyPartPickerApi::class.java)
    }

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize the Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "component_db"
        ).build()
    }
}