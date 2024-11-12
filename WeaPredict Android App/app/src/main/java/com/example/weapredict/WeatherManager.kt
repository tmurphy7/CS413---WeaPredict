package com.example.weapredict

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object WeatherManager {

    data class WeatherInstance(
        var weather_type: String = "Unknown Weather",
        var temperature_high: Double = 0.0,
        var temperature_low: Double = 0.0,
        var day: String = "Unknownsday", // Unsure if day and time will be needed
        var time: String = "99:99",
        var hour: String = "1AM"
    )

    data class AdditionalDataInstance(
        var wind_speed: Double = 0.0,
        var sunrise: String = "99:99",
        var sunset: String = "99:99"
    )

    private lateinit var requestQueue: RequestQueue

    // You MUST initialize WeatherFinder or else API calls will not work
    fun initialize(context: Context) {
        if (!::requestQueue.isInitialized)
        {
            requestQueue = Volley.newRequestQueue(context.applicationContext)
        }
    }

    fun requestLiveWeatherData(latitude: String, longitude: String, callback: (WeatherInstance) -> Unit) {
        if (!::requestQueue.isInitialized) {
            throw IllegalStateException("RequestQueue not initialized. Call WeatherFinder.initialize(context) first.")
        }

        // Currently requests live temperature and weather code for a specific region
        val baseUrl = "https://api.open-meteo.com/v1/forecast"
        val locationParams = "latitude=$latitude&longitude=$longitude"
        val currentParams = "current=temperature_2m,weather_code"
        val additionalParams = "timezone=auto&temperature_unit=fahrenheit"

        val url = "$baseUrl?$locationParams&$currentParams&$additionalParams"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val parsedWeather = parseWeatherResponse(response)
                callback(parsedWeather)
            },
            { error ->
                Log.d("Debug", "Error: ${error.toString()}")
                callback(WeatherInstance())
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    // Converts JSON response into a temperature and weather type using a weather code
    private fun parseWeatherResponse(response: JSONObject): WeatherInstance {
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

        // Set temperatureHigh AND temperatureLow as the "current" temperature
        return WeatherInstance(weatherCondition, temperature, temperature)
    }

    fun requestAdditionalData(latitude: String, longitude: String, callback: (AdditionalDataInstance) -> Unit) {
        if (!::requestQueue.isInitialized) {
            throw IllegalStateException("RequestQueue not initialized. Call WeatherFinder.initialize(context) first.")
        }

        // Request additional data for a specific region
        val baseUrl = "https://api.open-meteo.com/v1/forecast"
        val locationParams = "latitude=$latitude&longitude=$longitude"
        val currentParams = "current=wind_speed_10m&daily=sunrise,sunset"
        val additionalParams = "timezone=auto&temperature_unit=fahrenheit"

        val url = "$baseUrl?$locationParams&$currentParams&$additionalParams"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                val parsedData = parseAdditionalDataResponse(response)
                callback(parsedData)
            },
            { error ->
                Log.d("Debug", "Error: ${error.toString()}")
                callback(AdditionalDataInstance())
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    private fun parseAdditionalDataResponse(response: JSONObject): AdditionalDataInstance {
        val current = response.getJSONObject("current")
        val windSpeed = current.getDouble("wind_speed_10m")

        val daily = response.getJSONObject("daily")
        val sunriseArray = daily.getJSONArray("sunrise")
        val sunrise = extractTime(sunriseArray.getString(0))
        val sunsetArray = daily.getJSONArray("sunset")
        val sunset = extractTime(sunsetArray.getString(0))

        return AdditionalDataInstance(windSpeed, sunrise, sunset)
    }

    fun extractTime(datetime: String): String {
        // Define the input format to parse the datetime string
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val dateTime = LocalDateTime.parse(datetime, inputFormatter)

        // Define the output format to only display the time
        val outputFormatter = DateTimeFormatter.ofPattern("HH:mm")
        return dateTime.format(outputFormatter)
    }
}