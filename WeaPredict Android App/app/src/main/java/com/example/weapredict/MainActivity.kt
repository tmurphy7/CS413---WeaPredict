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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.weapredict.ui.theme.WeaPredictTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight


class MainActivity : ComponentActivity() {

    private lateinit var user_settings: Settings

    private var currentWeatherData by mutableStateOf(WeatherManager.WeatherInstance())
    private var locationStringState by mutableStateOf("Checking permissions...")
    private var additionalWeatherData by mutableStateOf(WeatherManager.AdditionalDataInstance())

    private val hourlyWeatherDataList = mutableStateListOf<WeatherManager.WeatherInstance>().apply {
            addAll(List(24) { WeatherManager.WeatherInstance() })
    }
    private val dailyWeatherDataList = mutableStateListOf<WeatherManager.WeatherInstance>().apply {
        addAll(List(7) { WeatherManager.WeatherInstance() })
    }

    private var locationServicesEnabled = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        // Create a blank UI while data loads
        enableEdgeToEdge()
        setContent { }

        WeatherManager.initialize(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        user_settings = Settings(this)
        refreshUI()
        checkPermissionAndFetchLocation()
    }

    private fun refreshUI() {
        val syncopateFont = FontAndColorManager.getSyncopate()
        val lexendDecaFont = FontAndColorManager.getLexendDeca()

        FontAndColorManager.refreshColorPalette(currentWeatherData, additionalWeatherData)
        val backgroundColor = FontAndColorManager.backgroundColor
        val foregroundColor = FontAndColorManager.foregroundColor
        val majorTextColor = FontAndColorManager.majorTextColor
        val minorTextColor = FontAndColorManager.minorTextColor

        enableEdgeToEdge()
        setContent {
            WeaPredictTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = backgroundColor
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
                                containerColor = Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp // Remove shadow
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = locationStringState,
                                    fontFamily = lexendDecaFont,
                                    color = majorTextColor,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "${currentWeatherData.weather_type}, ${currentWeatherData.temperature_high} °F",
                                    fontFamily = syncopateFont,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    color = majorTextColor,
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
                                    containerColor = foregroundColor
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(1.dp)
                                ) {
                                    Text(
                                        text = "Daily Forecast",
                                        color = minorTextColor,
                                        fontFamily = lexendDecaFont,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(16.dp, top = 16.dp)
                                    )
                                    UserInterfaceManager.DisplayDays(dailyWeatherDataList)
                                }
                            }

                            // Hourly Forecast Section
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = foregroundColor
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(1.dp)
                                ) {
                                    Text(
                                        text = "Hourly Forecast",
                                        color = minorTextColor,
                                        fontFamily = lexendDecaFont,
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(16.dp, top = 16.dp)
                                    )
                                    UserInterfaceManager.DisplayHours(
                                        currentWeatherData,
                                        additionalWeatherData,
                                        hourlyWeatherDataList)
                                }
                            }
                            UserInterfaceManager.CustomWeatherSquares(user_settings, additionalWeatherData)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Refresh Button
                        UserInterfaceManager.FindLocationButton(
                            onClick = { refreshUI() }
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
                        ModelManager.refreshWeatherPredictions(this, currentWeatherData, dailyWeatherDataList, hourlyWeatherDataList)
                        refreshUI()
                    }

                    WeatherManager.requestAdditionalData("$latitude", "$longitude") { additionalData ->
                        // Update the state variable directly
                        additionalWeatherData = additionalData
                        refreshUI()
                    }
                },
                onFailure = { error ->
                    updateLocationString("Error: ${error.message}")
                }
            )
        }
    }

    private fun updateLocationString(newLocation: String) {
        locationStringState = "Current weather in $newLocation"
    }
}