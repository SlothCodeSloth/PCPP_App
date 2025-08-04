package com.example.pcpartpicker

import android.content.Context
import android.preference.PreferenceManager

object SettingsDataManager {
    private const val PREF_REGION_KEY = "selected_region"
    private const val PREF_NAME_KEY = "user_name"
    private const val DEFAULT_REGION = "United States"
    private const val DEFAULT_NAME = ""

    private val regionToCurrencyMap = mapOf(
        "Australia" to "$",
        "Belgium" to "€",
        "Canada" to "$",
        "Czech Republic" to "Kč",
        "Denmark" to "kr",
        "Finland" to "€",
        "France" to "€",
        "Germany" to "€",
        "Hungary" to "Ft",
        "Ireland" to "€",
        "Italy" to "€",
        "Netherlands" to "€",
        "New Zealand" to "$",
        "Norway" to "kr",
        "Portugal" to "€",
        "Romania" to "lei",
        "Saudi Arabia" to "﷼",
        "Slovakia" to "€",
        "Spain" to "€",
        "Sweden" to "kr",
        "United Kingdom" to "£",
        "United States" to "$"
    )

    private val regionToCodeMap = mapOf(
        "Australia" to "au",
        "Austria" to "at",
        "Belgium" to "be",
        "Canada" to "ca",
        "Czech Republic" to "cz",
        "Denmark" to "dk",
        "Finland" to "fi",
        "France" to "fr",
        "Germany" to "de",
        "Hungary" to "hu",
        "Ireland" to "ie",
        "Italy" to "it",
        "Netherlands" to "nl",
        "New Zealand" to "nz",
        "Norway" to "no",
        "Portugal" to "pt",
        "Romania" to "ro",
        "Saudi Arabia" to "sa",
        "Slovakia" to "sk",
        "Spain" to "es",
        "Sweden" to "se",
        "United Kingdom" to "uk",
        "United States" to "us"
    )

    // Region Management
    fun saveRegion(context: Context, region: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PREF_REGION_KEY, region)
            .apply()
    }

    fun getSavedRegion(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_REGION_KEY, DEFAULT_REGION) ?: DEFAULT_REGION
    }

    // User Management
    fun saveName(context: Context, name: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(PREF_NAME_KEY, name)
            .apply()
    }

    fun getSavedName(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_NAME_KEY, DEFAULT_NAME) ?: DEFAULT_NAME
    }

    // Currency Retreival
    fun getCurrencySymbol(context: Context): String {
        val savedRegion = getSavedRegion(context)
        return regionToCurrencyMap[savedRegion] ?: "$"
    }

    fun getRegionCode(context: Context): String {
        val savedRegion = getSavedRegion(context)
        return regionToCodeMap[savedRegion] ?: "us"
    }
}
