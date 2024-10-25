package com.example.weapredict

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import java.util.Calendar

object UserInterfaceManager {

    @Composable
    fun DisplayDays(currentWeatherData : WeatherManager.WeatherInstance){
        //Display the weather for 7 day forecast
        Row(modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth()) {

            val weatherObjectDaysList = getDays(currentWeatherData)

            for (day in weatherObjectDaysList) {
                //set image for weather type
                val weatherCondition = when (day.weather_type) {
                    "Clear sky" -> R.drawable.sun
                    "Partly cloudy" -> R.drawable.parlycloudy
                    "Foggy" -> R.drawable.fog
                    "Drizzle" -> R.drawable.lightrain
                    "Rain showers","Rain" -> R.drawable.heavyrain
                    "Snow","Snow showers","Snow grains" -> R.drawable.snow
                    "Thunderstorm" -> R.drawable.storm
                    "Thunderstorm with hail" -> R.drawable.stormwithheavyrain
                    else -> R.drawable.sun
                }
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = day.day,
                        modifier = Modifier.padding(8.dp)
                    )
                    Box(modifier = Modifier.height(50.dp)) {
                        Image(
                            modifier = Modifier.size(60.dp),
                            contentDescription = "Weather Image",
                            contentScale = ContentScale.Crop,
                            painter = painterResource(weatherCondition)
                        )
                    }
                    Text(
                        text = "H: " + day.temperature_high.toString() + "\u00B0\n L:" + day.temperature_low + "\u00B0",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun DisplayHours(currentWeatherData : WeatherManager.WeatherInstance) {
        Row(modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth()) {

            val weatherObjectHourList = getHours(currentWeatherData)
            //set image for weather type
            for (hour in weatherObjectHourList) {
                val weatherCondition = when (hour.weather_type) {
                    "Clear sky" -> R.drawable.sun
                    "Partly cloudy" -> R.drawable.parlycloudy
                    "Foggy" -> R.drawable.fog
                    "Drizzle" -> R.drawable.lightrain
                    "Rain showers","Rain" -> R.drawable.heavyrain
                    "Snow","Snow showers","Snow grains" -> R.drawable.snow
                    "Thunderstorm" -> R.drawable.storm
                    "Thunderstorm with hail" -> R.drawable.stormwithheavyrain
                    else -> R.drawable.sun
                }

                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = hour.hour,
                        modifier = Modifier.padding(8.dp)
                    )
                    Box(modifier = Modifier.height(50.dp)) {
                        Image(
                            modifier = Modifier.size(60.dp),
                            contentDescription = "Weather Image",
                            contentScale = ContentScale.Crop,
                            painter = painterResource(weatherCondition)
                        )
                    }
                    Text(
                        text = hour.temperature_high.toString() + "\u00B0",
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

    }
    private fun getDays(currentWeatherData : WeatherManager.WeatherInstance): List<WeatherManager.WeatherInstance>{
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
        val temperature_h = currentWeatherData.temperature_high
        val temperature_l = currentWeatherData.temperature_low
        val skies = currentWeatherData.weather_type
        val todaysWeather = WeatherManager.WeatherInstance(weather_type = skies,temperature_high = temperature_h, temperature_low = temperature_l, day = daysList[0])

        //drop first element from dayslist
        daysList = daysList.drop(1)

        weatherObjectList = weatherObjectList + todaysWeather
//for loop through days creating weather objects and adding to list
        for(x in daysList){
            val nextDayTempH = 70.0 // predict temperature
            val nextDayTempL = 50.0
            val nextDaySkies = "Rain" // predict conditions
            val nextDayWeather = WeatherManager.WeatherInstance(weather_type = nextDaySkies,temperature_high = nextDayTempH, temperature_low = nextDayTempL, day = x)
            weatherObjectList = weatherObjectList + nextDayWeather
        }

        return weatherObjectList
    }

    private fun getHours(currentWeatherData : WeatherManager.WeatherInstance): List<WeatherManager.WeatherInstance>{
        var hoursList = listOf("12AM","1AM","2AM","3AM","4AM","5AM","6AM","7AM","8AM","9AM","10AM","11AM","12PM","1PM",
            "2PM","3PM","4PM","5PM","6PM","7PM","8PM","9PM","10PM","11PM")

        //get current hour
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)

        //get current weather
        val temperature_h = currentWeatherData.temperature_high
        val skies = currentWeatherData.weather_type

        //sort list of hours into correct order
        if(currentHour != 0) {
            val hoursListSecondHalf = hoursList.subList(0, currentHour)
            val hoursListFirstHalf = hoursList.subList(currentHour, hoursList.size)
            hoursList = hoursListFirstHalf + hoursListSecondHalf
        }

        var weatherObjectList: List<WeatherManager.WeatherInstance> = emptyList()
        //for loop through days creating weather objects and adding to list
        val todaysWeather = WeatherManager.WeatherInstance(weather_type = skies,temperature_high = temperature_h, hour = hoursList[0])

        //drop first element from hourslist
        hoursList = hoursList.drop(1)

        weatherObjectList = weatherObjectList + todaysWeather

        for(x in hoursList){
            val nextDayTempH = 70.0 // predict temperature
            val nextDaySkies = "Rain" // predict conditions
            val nextDayWeather = WeatherManager.WeatherInstance(weather_type = nextDaySkies,temperature_high = nextDayTempH, hour = x)
            weatherObjectList = weatherObjectList + nextDayWeather
        }

        return weatherObjectList
    }

    @Composable
    fun FindLocationButton(onClick: () -> Unit) {
        Button(onClick = { onClick() }) {
            Text("Refresh Data")
        }
    }
}