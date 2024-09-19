package com.example.weapredict

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.weapredict.ui.theme.WeaPredictTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.weapredict.LocationFinder

class MainActivity : ComponentActivity() {

    // Prepare to read location from user
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Google Play service used to get location data
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Create the UI
        enableEdgeToEdge()
        setContent {
            WeaPredictTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // String will update automatically once location is returned
                    var locationString by remember { mutableStateOf("Fetching location...") }

                    // Returns the location of the user when available
                    LocationFinder.findLocation(fusedLocationClient, this) { locationPair ->
                        locationString = "${locationPair.first}, ${locationPair.second}"
                    }

                    LocationTest(
                        name = locationString,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
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