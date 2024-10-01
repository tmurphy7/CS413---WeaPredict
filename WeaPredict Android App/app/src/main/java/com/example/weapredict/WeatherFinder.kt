package com.example.weapredict

import android.app.Activity
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

object WeatherFinder {

    fun requestLiveWeatherData (activity: Activity, latitude: String, longitude: String): String {

        var dataString = "Data not found."

        // Instantiate the RequestQueue with the cache and network. Start the queue.
        val requestQueue = Volley.newRequestQueue(activity)

        // Placeholder API
        val locationForUrl = "?latitude=$latitude&longitude=$longitude"
        val dataForUrl = "&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m"
        val url = "https://api.open-meteo.com/v1/forecast$locationForUrl$dataForUrl"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.d("Debug","Response: ".format(response.toString()))
                dataString = response.toString()
            },
            { error ->
                Log.d("Debug","Error: ".format(error.toString()))
            }
        )

        requestQueue.add(jsonObjectRequest)

        return dataString
    }
}