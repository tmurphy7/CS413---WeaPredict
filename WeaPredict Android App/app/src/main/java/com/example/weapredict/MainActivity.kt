package com.example.weapredict

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.weapredict.ui.theme.WeaPredictTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private var currentWeatherData = WeatherManager.WeatherInstance()

    private var locationServicesEnabled = true

    // Handles the location API
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Handles requesting location services from the user
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchLocationAndLiveWeather()
            } else {
                updateLocationString("Location permissions are required to use WeaPredict." +
                " To use WeaPredict, please enable location services in your phone's settings.")
                updateWeatherString("Current weather unavailable.")
            }
        }

    // Strings that update the UI automatically when location data and/or weather data is returned
    private var locationStringState by mutableStateOf("Checking permissions...")
    private var weatherStringState by mutableStateOf("Awaiting weather data...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize necessary services
        WeatherManager.initialize(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createUI()

        // After the UI is created, request location permissions
        checkPermissionAndFetchLocation()
    }

    private fun createUI() {
        enableEdgeToEdge()
        setContent {
            WeaPredictTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        UserInterfaceManager.FindLocationButton(onClick = { checkPermissionAndFetchLocation() })
                        UserInterfaceManager.DisplayString(
                            string = locationStringState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        UserInterfaceManager.DisplayString(
                            string = weatherStringState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // NOTE: Check doesn't actually do anything yet
                        if (locationServicesEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            UserInterfaceManager.DisplayDays(currentWeatherData)

                            Spacer(modifier = Modifier.height(16.dp))
                            UserInterfaceManager.DisplayHours(currentWeatherData)
                        }
                    }
                }
            }
        }
    }

    private fun checkPermissionAndFetchLocation() {
        when {
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocationAndLiveWeather() // If permission was already granted, fetch data
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                updateLocationString("Location permission is required to use WeaPredict." +
                        " To use WeaPredict, please enable location services in your phone's settings.")
                updateWeatherString("Current weather unavailable.")
            } // If permissions were denied, don't request location and display an error message
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } // If neither of the previous conditions were true, request the location services
        }
    }

    private fun fetchLocationAndLiveWeather() {
        updateLocationString("Fetching location...")
        LocationManager.findLocation(fusedLocationClient, this) { result ->
            result.fold( // Handles success and failure of findLocation function
                onSuccess = { (latitude, longitude) ->
                    val locationString = LocationManager.getLocationAsAddress(this, latitude, longitude)
                    updateLocationString(locationString)

                    // Call requestLiveWeatherData with a callback
                    WeatherManager.requestLiveWeatherData("$latitude", "$longitude") { weatherData ->
                        // Update weather data on the UI
                        currentWeatherData = weatherData
                        val currentWeather = currentWeatherData.weather_type
                        val currentTemperature = currentWeatherData.temperature
                        updateWeatherString("$currentWeather, $currentTemperature °F")
                    }
                },
                onFailure = { error ->
                    updateLocationString("Error: ${error.message}")
                }
            )
        }
    }

    private fun updateLocationString(newLocation: String) {
        locationStringState = newLocation
    }

    private fun updateWeatherString(newWeather: String) {
        weatherStringState = newWeather
    }
}
