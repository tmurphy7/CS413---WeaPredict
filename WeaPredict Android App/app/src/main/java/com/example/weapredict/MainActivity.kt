package com.example.weapredict

import android.Manifest
import android.R.attr.contentDescription
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import java.util.Calendar


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
                        FindLocationButton(onClick = { checkPermissionAndFetchLocation() })
                        DisplayString(
                            string = locationStringState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        DisplayString(
                            string = weatherStringState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // NOTE: Check doesn't actually do anything yet
                        if (locationServicesEnabled)
                        {
                            Spacer(modifier = Modifier.height(16.dp))
                            DisplayDays()

                            Spacer(modifier = Modifier.height(16.dp))
                            DisplayHours()
                        }
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
@Composable
fun DisplayDays(){
    //Display the weather for 7 day forecast
    Row(modifier = Modifier
        .horizontalScroll(rememberScrollState())
        .fillMaxWidth()) {

        val weatherObjectDaysList = getDays()
        
        for (day in weatherObjectDaysList) {
            //set image for weather type
            val weatherCondition = when (day.weather_type) {
                "Clear \nsky" -> R.drawable.sun
                "Partly \ncloudy" -> R.drawable.parlycloudy
                "Foggy" -> R.drawable.fog
                "Drizzle" -> R.drawable.lightrain
                "Rain showers","Rain" -> R.drawable.heavyrain
                "Snow","Snow showers","Snow grains" -> R.drawable.snow
                "Thunderstorm" -> R.drawable.storm
                "Thunderstorm with hail" -> R.drawable.stormwithheavyrain
                else -> R.drawable.sun
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Box(modifier = Modifier.height(50.dp)) {
                    Image(
                        modifier = Modifier.size(60.dp),
                        contentDescription = "Weather Image",
                        contentScale = ContentScale.Crop,
                        painter = painterResource(weatherCondition)
                    )
                }
                Text(
                    text = day.day + "\n" + day.temperature + "\n" + day.weather_type,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun DisplayHours() {
    Row(modifier = Modifier
        .horizontalScroll(rememberScrollState())
        .fillMaxWidth()) {

        val weatherObjectHourList = getHours()
        //set image for weather type
        for (hour in weatherObjectHourList) {
            val weatherCondition = when (hour.weather_type) {
                "Clear \nsky" -> R.drawable.sun
                "Partly \ncloudy" -> R.drawable.parlycloudy
                "Foggy" -> R.drawable.fog
                "Drizzle" -> R.drawable.lightrain
                "Rain showers","Rain" -> R.drawable.heavyrain
                "Snow","Snow showers","Snow grains" -> R.drawable.snow
                "Thunderstorm" -> R.drawable.storm
                "Thunderstorm with hail" -> R.drawable.stormwithheavyrain
                else -> R.drawable.sun
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Box(modifier = Modifier.height(50.dp)) {
                    Image(
                        modifier = Modifier.size(60.dp),
                        contentDescription = "Weather Image",
                        contentScale = ContentScale.Crop,
                        painter = painterResource(weatherCondition)
                    )
                }
                Text(
                    text = hour.hour + "\n" + hour.temperature + "\n" + hour.weather_type,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

}
fun getDays(): List<WeatherManager.WeatherInstance>{
    //create 7 weather objects and make predictions to fill weather
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

    var daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    //Split the list to start from today
    if(dayOfWeek != 0){
        val daysListSecondHalf = daysList.subList(0,dayOfWeek - 1)
        val daysListFirstHalf = daysList.subList(dayOfWeek - 1,daysList.size)
        daysList = daysListFirstHalf + daysListSecondHalf
    }

    var weatherObjectList: List<WeatherManager.WeatherInstance> = emptyList()

    //add first day to list
    val temperature = 68.0 // Get temp and conditions from api
    val skies = "Clear \nsky"
    val todaysWeather = WeatherManager.WeatherInstance(weather_type = skies,temperature = temperature, day = daysList[0])

    //drop first element from dayslist
    daysList = daysList.drop(1)

    weatherObjectList = weatherObjectList + todaysWeather
//for loop through days creating weather objects and adding to list
    for(x in daysList){
        val nextDayTemp = 70.0 // predict temperature
        val nextDaySkies = "Rain" // predict conditions
        val nextDayWeather = WeatherManager.WeatherInstance(weather_type = nextDaySkies,temperature = nextDayTemp, day = x)
        weatherObjectList = weatherObjectList + nextDayWeather
    }

    return weatherObjectList
}

fun getHours(): List<WeatherManager.WeatherInstance>{
    var hoursList = listOf("12AM","1AM","2AM","3AM","4AM","5AM","6AM","7AM","8AM","9AM","10AM","11AM","12PM","1PM",
        "2PM","3PM","4PM","5PM","6PM","7PM","8PM","9PM","10PM","11PM")

    //get current hour
    val currentTime = Calendar.getInstance()
    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)

    //get current weather
    val temperature = 68.0
    val skies = "Partly \ncloudy"

    //sort list of hours into correct order
    if(currentHour != 0){
        val hoursListSecondHalf = hoursList.subList(0,currentHour)
        val hoursListFirstHalf = hoursList.subList(currentHour,hoursList.size)
        hoursList = hoursListFirstHalf + hoursListSecondHalf
    }

    var weatherObjectList: List<WeatherManager.WeatherInstance> = emptyList()
    //for loop through days creating weather objects and adding to list
    val todaysWeather = WeatherManager.WeatherInstance(weather_type = skies,temperature = temperature, hour = hoursList[0])

    //drop first element from hourslist
    hoursList = hoursList.drop(1)


    weatherObjectList = weatherObjectList + todaysWeather

    for(x in hoursList){
        val nextDayTemp = 70.0 // predict temperature
        val nextDaySkies = "Rain" // predict conditions
        val nextDayWeather = WeatherManager.WeatherInstance(weather_type = nextDaySkies,temperature = nextDayTemp, hour = x)
        weatherObjectList = weatherObjectList + nextDayWeather
    }

    return weatherObjectList
}


@Composable
fun DisplayString(string: String, modifier: Modifier = Modifier) {
    Text(
        text = string,
        modifier = modifier
    )
}

@Composable
fun FindLocationButton(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Refresh Data")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeaPredictTheme {
        DisplayString("Location Unknown")
    }
}
