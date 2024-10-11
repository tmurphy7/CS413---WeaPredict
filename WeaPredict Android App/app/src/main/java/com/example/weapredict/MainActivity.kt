package com.example.weapredict

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.weapredict.ui.theme.WeaPredictTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import java.util.Calendar

class MainActivity : ComponentActivity() {

    // Handles the location API
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Handles requesting location services from the user
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchLocationAndLiveWeather()
            } else {
                updateLocationString("Permission denied")
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

        // Creating the UI
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
                        DisplayString(
                            string = locationStringState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        DisplayString(
                            string = weatherStringState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        DisplayDays()

                        Spacer(modifier = Modifier.height(16.dp))
                        DisplayHours()
                    }
                }
            }
        }

        // After the UI is created, request location permissions
        checkPermissionAndFetchLocation()
    }

    private fun checkPermissionAndFetchLocation() {
        when {
            ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocationAndLiveWeather() // If permission was already granted, fetch data
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                updateLocationString("Location permission is required to use WeaPredict.")
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
                        val currentWeather = weatherData.weather_type
                        val currentTemperature = weatherData.temperature
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
@Composable
fun DisplayDays(){
    Row(modifier = Modifier
        .horizontalScroll(rememberScrollState())
        .fillMaxWidth()) {

        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        var daysList = getDays()

        val temperature = "68\u2109"
        val skies = "Clear Skies"

        //Split the list to start from today
        if(dayOfWeek != 0){
            val daysListSecondHalf = daysList.subList(0,dayOfWeek - 1)
            val daysListFirstHalf = daysList.subList(dayOfWeek - 1,daysList.size)
            daysList = daysListFirstHalf + daysListSecondHalf
        }
        
        for (day in daysList){
            Text(
                text = day + "\n" + temperature + "\n" + skies,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

fun getDays(): List<String>{
    return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
}
@Composable
fun DisplayHours() {
    Row(modifier = Modifier
        .horizontalScroll(rememberScrollState())
        .fillMaxWidth()) {

        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        var hoursList = getHours()

        val temperature = "68\u2109"
        val skies = "Partly Cloudy"

        if(currentHour != 0){
            val hoursListSecondHalf = hoursList.subList(0,currentHour)
            val hoursListFirstHalf = hoursList.subList(currentHour,hoursList.size)
            hoursList = hoursListFirstHalf + hoursListSecondHalf
        }

        for (hour in hoursList){
            Text(
                text = hour + "\n" + temperature + "\n" +  skies,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

}

fun getHours(): List<String>{
    return listOf("12AM","1AM","2AM","3AM","4AM","5AM","6AM","7AM","8AM","9AM","10AM","11AM","12PM","1PM",
        "2PM","3PM","4PM","5PM","6PM","7PM","8PM","9PM","10PM","11PM")
}


@Composable
fun DisplayString(string: String, modifier: Modifier = Modifier) {
    Text(
        text = string,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeaPredictTheme {
        DisplayString("Location Unknown")
    }
}
