package com.example.weapredict

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.weapredict.ui.theme.WeaPredictTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.tensorflow.lite.Interpreter
import java.util.Date

class MainActivity : ComponentActivity() {

    private var currentWeatherData by mutableStateOf(WeatherManager.WeatherInstance())
    private var locationStringState by mutableStateOf("Checking permissions...")

    private var locationServicesEnabled = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var temperatureModelName = "temperatureModel.tflite"
    private lateinit var temperatureModel: Interpreter
    private var weatherModelName = "weatherClass.tflite"
    private lateinit var weatherModel: Interpreter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchLocationAndLiveWeather()
            } else {
                updateLocationString("Location permissions are required to use WeaPredict." +
                        " To use WeaPredict, please enable location services in your phone's settings.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WeatherManager.initialize(this)

        val currentDateTime = Date()
        Log.d("DEBUG", currentDateTime.toString())

        // TODO: Temperature model testing for R
        // temperatureModel = ModelManager.loadModelFromAssetsFolder(temperatureModelName, this)
        // val temperatureModelTestInput = currentDateTime
        // val temperatureModelTestOutput = FloatArray(1)
        // temperatureModel.run(temperatureModelTestInput, temperatureModelTestOutput)
        // Log.d("DEBUG", temperatureModelTestOutput.toString())

        // TODO: Weather code model testing for T
        weatherModel = ModelManager.loadModelFromAssetsFolder(weatherModelName, this)
        val weatherModelTestInput = 54.5f // Example float input for temperature
        Log.d("DEBUG", ModelManager.predictWeatherClass(weatherModelTestInput, weatherModel))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createUI()
        checkPermissionAndFetchLocation()
    }

    private fun createUI() {
        enableEdgeToEdge()
        setContent {
            WeaPredictTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Current Weather
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "Current Weather",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "${currentWeatherData.weather_type}, ${currentWeatherData.temperature_high} °F",
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (locationServicesEnabled) {
                            // Daily Forecast Section
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Daily Forecast",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    UserInterfaceManager.DisplayDays(currentWeatherData)
                                }
                            }

                            // Hourly Forecast Section
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Hourly Forecast",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    UserInterfaceManager.DisplayHours(currentWeatherData)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Location Display
                        Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = locationStringState,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.fillMaxWidth()
                                )
                        }

                        // Refresh Button
                        UserInterfaceManager.FindLocationButton(
                            onClick = { checkPermissionAndFetchLocation() }
                        )
                    }
                }
            }
        }
    }

    private fun checkPermissionAndFetchLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocationAndLiveWeather() // If permission was already granted, fetch data
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                updateLocationString(
                    "Location permission is required to use WeaPredict." +
                            " To use WeaPredict, please enable location services in your phone's settings."
                )
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchLocationAndLiveWeather() {
        updateLocationString("Fetching location...")
        LocationManager.findLocation(fusedLocationClient, this) { result ->
            result.fold(
                onSuccess = { (latitude, longitude) ->
                    val locationString = LocationManager.getLocationAsAddress(this, latitude, longitude)
                    updateLocationString(locationString)

                    WeatherManager.requestLiveWeatherData("$latitude", "$longitude") { weatherData ->
                        // Update the state variable directly
                        currentWeatherData = weatherData
                    }
                },
                onFailure = { error ->
                    updateLocationString("Error: ${error.message}")
                }
            )
        }
    }

    private fun updateLocationString(newLocation: String) {
        locationStringState = "Location: $newLocation"
    }
}