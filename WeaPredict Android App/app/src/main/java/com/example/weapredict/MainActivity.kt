package com.example.weapredict

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import java.util.Calendar

class MainActivity : ComponentActivity() {

    // Handles the location API
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Handles requesting location services from the user
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                fetchLocation()
            } else {
                updateLocationString("Permission denied")
            }
        }

    // String that updates the UI automatically when location data is returned
    private var locationStringState by mutableStateOf("Checking permissions...")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Creating the UI
        enableEdgeToEdge()
        setContent {
            WeaPredictTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationTest(
                        name = locationStringState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                DisplayDays()
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
                fetchLocation() // If permission was already granted, fetch the location
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                updateLocationString("Location permission is required to use WeaPredict.")
            } // If permissions were denied, don't request location and display an error message
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } // If neither of the previous conditions were true, request the location services
        }
    }

    private fun fetchLocation() {
        updateLocationString("Fetching location...")
        LocationFinder.findLocation(fusedLocationClient, this) { result ->
            result.fold( // Handles success and failure of findLocation function
                onSuccess = { (latitude, longitude) ->
                    val locationString = LocationFinder.getLocationAsAddress(this, latitude, longitude)
                    updateLocationString("$latitude, $longitude, AKA $locationString")

                    // TODO: Needs to place string, this is basically placeholder implementation
                    WeatherFinder.requestLiveWeatherData(this, "$latitude", "$longitude")
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
}
@Composable
fun DisplayDays(){
    Row(modifier = Modifier
        .horizontalScroll(rememberScrollState())
        .fillMaxWidth()) {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var daysList = getDays()

        //Split the list to start from today
        val daysListSecondHalf = daysList.subList(0,dayOfWeek - 2)
        val daysListFirstHalf = daysList.subList(dayOfWeek -2,daysList.size)
        daysList = daysListFirstHalf + daysListSecondHalf

        for (day in daysList){
            Text(
                text = day,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

fun getDays(): List<String>{
    return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
}

@Composable
fun LocationTest(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Current Location: $name",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeaPredictTheme {
        LocationTest("Location Unknown")
    }
}
