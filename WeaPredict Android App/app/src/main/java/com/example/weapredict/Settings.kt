package com.example.weapredict
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.nio.file.Path

class Settings(context: Context) {
    var number_of_widgets = 0
    var list_of_widgets = mutableListOf("")

    val file = File(context.filesDir, "settings.json")

    init{
        if(file.exists()){
            loadSettings()
        }
        else{
            number_of_widgets = 0
            list_of_widgets = mutableListOf("")
            saveSettings()
        }
    }

    fun saveSettings() {
        val gson = Gson()
        val jsonString = gson.toJson(this)
        Log.d("SaveSettings", "JSON to save: $jsonString")

        try {
            file.writeText(jsonString)  // Write the JSON string to the file

            // Confirm that the file was written by logging the file path
            Log.d("SaveSettings", "Settings saved successfully at ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("SaveSettings", "Failed to save settings", e)
        }
    }

    fun loadSettings(): Settings? {
        if (file.exists()) {
            val jsonString = file.readText()
            // Use Gson to convert the JSON string back to the Settings object
            val gson = Gson()
            val loadedSettings: Settings = gson.fromJson(jsonString, Settings::class.java)

            this.number_of_widgets = loadedSettings.number_of_widgets
            this.list_of_widgets = loadedSettings.list_of_widgets
        }
        return null
    }

}