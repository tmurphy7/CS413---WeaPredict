package com.example.weapredict

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object WeatherFinder {

    private lateinit var requestQueue: RequestQueue

    // You MUST initialize WeatherFinder or else API calls will not work
    fun initialize(context: Context) {
        if (!::requestQueue.isInitialized)
        {
            requestQueue = Volley.newRequestQueue(context.applicationContext)
        }
    }

    fun requestLiveWeatherData(latitude: String, longitude: String, callback: (String) -> Unit) {
        if (!::requestQueue.isInitialized) {
            throw IllegalStateException("RequestQueue not initialized. Call WeatherFinder.initialize(context) first.")
        }

        // Currently requests live temperature and weather code for a specific region
        val baseUrl = "https://api.open-meteo.com/v1/forecast"
        val locationParams = "latitude=$latitude&longitude=$longitude"
        val currentParams = "current=temperature_2m,weather_code"
        val additionalParams = "timezone=auto"

        val url = "$baseUrl?$locationParams&$currentParams&$additionalParams"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val parsedWeather = parseWeatherResponse(response)
                callback(parsedWeather)
            },
            { error ->
                Log.d("Debug", "Error: ${error.toString()}")
                callback("Weather data not available. Error: ${error.message}")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    // Converts JSON response into a temperature and weather type using a weather code
    private fun parseWeatherResponse(response: JSONObject): String {
        val current = response.getJSONObject("current")
        val temperature = current.getDouble("temperature_2m")
        val weatherCode = current.getInt("weather_code")

        val weatherCondition = when (weatherCode) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }

        return "$weatherCondition, $temperature °C"
    }
}