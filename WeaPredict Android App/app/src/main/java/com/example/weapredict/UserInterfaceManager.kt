package com.example.weapredict

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.stream.IntStream.range
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

object UserInterfaceManager {

    @Composable
    fun DisplayDays(dailyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>){
        //Display the weather for 7 day forecast
        val lexendDecaFont = FontManager.getLexendDeca()
        Row(modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth()) {

            val weatherObjectDaysList = getDays(dailyWeatherDataList)

            for (day in weatherObjectDaysList) {
                //set image for weather type
                val weatherCondition = getDrawableWeatherImage(day.weather_type, true)
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = day.day,
                        fontFamily = lexendDecaFont,
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
                        text = "H: " + day.temperature_high.toString() + "\u00B0\n L: " + day.temperature_low + "\u00B0",
                        fontFamily = lexendDecaFont,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun DisplayHours(currentWeatherData : WeatherManager.WeatherInstance,
                     additionalWeatherData : WeatherManager.AdditionalDataInstance,
                     hourlyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>
    ) {
        val lexendDecaFont = FontManager.getLexendDeca()

        // Get information needed for sunrise/sunset
        val sunriseTime = additionalWeatherData.sunrise // Formatted as string, XX:XX
        val sunriseHour =
            sunriseTime.split(":")[0].toInt() // Just the first integer (01:39 becomes 1)
        val sunsetTime = additionalWeatherData.sunset
        val sunsetHour = sunsetTime.split(":")[0].toInt()

        // Prepare to create UI object
        val weatherObjectHourList = getHours(currentWeatherData, hourlyWeatherDataList)
        var sunriseNotWritten = true
        var sunsetNotWritten = true
        var isCurrentlyDaytime = WeatherManager.isDaytime(
            sunriseHour,
            sunsetHour,
            convertHourToInteger(weatherObjectHourList[0].hour)
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {

            var i = 0
            while (i < 24) {
                val hour = weatherObjectHourList[i]
                val hourInt = convertHourToInteger(hour.hour)

                if (hourInt == sunriseHour + 1 && sunriseNotWritten) // If the sunrise needs to be printed
                {
                    val sunriseImage = getDrawableWeatherImage("Sunrise", true)

                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = convertTo12HourFormat(sunriseTime),
                            fontFamily = lexendDecaFont,
                            modifier = Modifier.padding(8.dp)
                        )
                        Box(modifier = Modifier.height(50.dp)) {
                            Image(
                                modifier = Modifier.size(60.dp),
                                contentDescription = "Weather Image",
                                contentScale = ContentScale.Crop,
                                painter = painterResource(sunriseImage)
                            )
                        }
                        Text(
                            text = "Sunrise",
                            fontFamily = lexendDecaFont,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    sunriseNotWritten = false
                    isCurrentlyDaytime = true
                } else if (hourInt == sunsetHour + 1 && sunsetNotWritten) {
                    val sunsetImage = getDrawableWeatherImage("Sunset", true)

                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = convertTo12HourFormat(sunsetTime),
                            fontFamily = lexendDecaFont,
                            modifier = Modifier.padding(8.dp)
                        )
                        Box(modifier = Modifier.height(50.dp)) {
                            Image(
                                modifier = Modifier.size(60.dp),
                                contentDescription = "Weather Image",
                                contentScale = ContentScale.Crop,
                                painter = painterResource(sunsetImage)
                            )
                        }
                        Text(
                            text = "Sunset",
                            fontFamily = lexendDecaFont,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    sunsetNotWritten = false
                    isCurrentlyDaytime = false
                } else // Just print the hour as usual
                {
                    val weatherCondition =
                        getDrawableWeatherImage(hour.weather_type, isCurrentlyDaytime)

                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = hour.hour,
                            fontFamily = lexendDecaFont,
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
                            fontFamily = lexendDecaFont,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    i++
                }
            }
        }
    }

    fun getDrawableWeatherImage(weatherType : String, isDaytime : Boolean): Int
    {
        val weatherCondition = when (weatherType) {
            "Clear sky" -> if (isDaytime) R.drawable.sun else R.drawable.moon
            "Partly cloudy" -> if (isDaytime) R.drawable.parlycloudy else R.drawable.partlycloudymoon
            "Foggy" -> if (isDaytime) R.drawable.fog else R.drawable.fogmoon
            "Drizzle" -> R.drawable.lightrain
            "Rain showers", "Rain" -> R.drawable.heavyrain
            "Snow", "Snow showers", "Snow grains" -> R.drawable.snow
            "Thunderstorm" -> R.drawable.storm
            "Thunderstorm with hail" -> R.drawable.stormwithheavyrain
            "Sunrise" -> R.drawable.sunrise
            "Sunset" -> R.drawable.sunset
            else -> R.drawable.sun
        }
        return weatherCondition
    }

    private fun getDays(
        dailyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>
    ): List<WeatherManager.WeatherInstance> {
        //create 7 weather objects and make predictions to fill weather
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        var daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        //Split the list to start from today
        if (dayOfWeek != 0) {
            val daysListSecondHalf = daysList.subList(0, dayOfWeek - 1)
            val daysListFirstHalf = daysList.subList(dayOfWeek - 1, daysList.size)
            daysList = daysListFirstHalf + daysListSecondHalf
        }

        var weatherObjectList: List<WeatherManager.WeatherInstance> = emptyList()

        //for loop through days creating weather objects and adding to list
        for (day in 0 until 7) {
            var nextDayTempH = dailyWeatherDataList[day].temperature_high
            nextDayTempH = String.format("%.1f", nextDayTempH.toFloat()).toDouble()
            var nextDayTempL = dailyWeatherDataList[day].temperature_low
            nextDayTempL = String.format("%.1f", nextDayTempL.toFloat()).toDouble()
            val nextDaySkies = dailyWeatherDataList[day].weather_type
            val nextDayWeather = WeatherManager.WeatherInstance(
                weather_type = nextDaySkies,
                temperature_high = nextDayTempH,
                temperature_low = nextDayTempL,
                day = daysList[day]
            )
            weatherObjectList = weatherObjectList + nextDayWeather
        }

        return weatherObjectList
    }

    private fun getHours(
        currentWeatherData : WeatherManager.WeatherInstance,
        hourlyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>
    ): List<WeatherManager.WeatherInstance>{
        var hoursList = listOf("12AM","1AM","2AM","3AM","4AM","5AM","6AM","7AM","8AM","9AM","10AM","11AM","12PM","1PM",
            "2PM","3PM","4PM","5PM","6PM","7PM","8PM","9PM","10PM","11PM")

        // Get current hour and weather data
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val temperature_h = currentWeatherData.temperature_high
        val skies = currentWeatherData.weather_type

        // Sort list of hours into correct order
        if(currentHour != 0) {
            val hoursListSecondHalf = hoursList.subList(0, currentHour)
            val hoursListFirstHalf = hoursList.subList(currentHour, hoursList.size)
            hoursList = hoursListFirstHalf + hoursListSecondHalf
        }

        var weatherObjectList: List<WeatherManager.WeatherInstance> = emptyList()

        val todaysWeather = WeatherManager.WeatherInstance(
            weather_type = skies,
            temperature_high = temperature_h,
            hour = hoursList[0]
        )

        // TODO: Review getHours()
        // Drop first element from hourslist - seems to break code?
        // hoursList = hoursList.drop(1)

        weatherObjectList = weatherObjectList + todaysWeather

        for(hour in 1 until 24){
            var nextHourTempH = hourlyWeatherDataList[hour].temperature_high
            nextHourTempH = String.format("%.1f", nextHourTempH.toFloat()).toDouble()
            var nextHourTempL = hourlyWeatherDataList[hour].temperature_low
            nextHourTempL = String.format("%.1f", nextHourTempL.toFloat()).toDouble()
            val nextHourSkies = hourlyWeatherDataList[hour].weather_type
            val nextHourWeather = WeatherManager.WeatherInstance(
                weather_type = nextHourSkies,
                temperature_high = nextHourTempH,
                temperature_low = nextHourTempL,
                hour = hoursList[hour])
            weatherObjectList = weatherObjectList + nextHourWeather
        }

        return weatherObjectList
    }

    @Composable
    fun FindLocationButton(onClick: () -> Unit) {
        Button(onClick = { onClick() }) {
            Text("Refresh Data")
        }
    }

    @Composable
    fun CustomWeatherSquares(settings: Settings, weatherData: WeatherManager.AdditionalDataInstance) {
        //Display the weather for 7 day forecast
        var expanded by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf("") }


        val squareListState = remember { mutableStateOf(settings.list_of_widgets.toList()) }

        val deleteModeState = remember { mutableStateMapOf<String, Boolean>() }

        squareListState.value.forEach { item ->
            if (!deleteModeState.containsKey(item)) {
                deleteModeState[item] = false
            }
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {

            //able to have more  to display
            settings.loadSettings()

            for (square_title in squareListState.value) {
                //set unit symbol to display
                val unit_symbol = when (square_title) {
                    //Need to verify these are correct units
                    "Humidity" -> "%"
                    "Rain Fall" -> "mm"
                    "Snow Fall" -> "cm"
                    "Wind Speed" -> "km/h"
                    "UV Index" -> ""
                    else -> "" // Default case
                }

                val weather_value = when (square_title) {
                    //Need to verify these are correct units
                    "Humidity" -> "" // weatherData.humidity
                    "Rain Fall" -> weatherData.rain_sum
                    "Snow Fall" -> "" // weatherData.snow_fall
                    "Wind Speed" -> weatherData.wind_speed
                    "UV Index" -> weatherData.uv_index
                    else -> "" // Default case
                }

                Box(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                deleteModeState[square_title] = true // Show delete button
                            }
                        )
                    }
                ) {
                    Card(
                        modifier = Modifier
                            .size(width = 210.dp, height = 190.dp)
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .padding(horizontal = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {

                            Text(
                                text = square_title + ": " + weather_value + unit_symbol, //I THINK THIS WORKS?,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.align(Alignment.Center)
                            )
                            if (deleteModeState[square_title] == true) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete), // Replace with your "remove" icon resource
                                    contentDescription = "Remove $square_title",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(50.dp)
                                        .clickable {
                                            // Remove the widget and update state
                                            settings.list_of_widgets.remove(square_title)
                                            settings.number_of_widgets--
                                            settings.saveSettings()
                                            deleteModeState[square_title] = false
                                            squareListState.value =
                                                settings.list_of_widgets.toList()
                                        }
                                )
                            }
                        }

                    }
                }
            }
            // Add button
            Card(
                modifier = Modifier.clickable { expanded = true }
                    .size(width = 210.dp, height = 190.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        modifier = Modifier.size(100.dp),
                        contentDescription = "Plus Sign",
                        contentScale = ContentScale.Crop,
                        painter = painterResource(R.drawable.plussign)
                    )
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false })
                {
                    val weather_data_list =
                        listOf("Humidity", "Rain Fall", "Snow Fall", "Wind Speed", "UV Index")
                    for (weather_data_type in weather_data_list) {
                        DropdownMenuItem(
                            text = { Text(weather_data_type) },
                            onClick = {
                                expanded = false
                                selectedItem = weather_data_type
                                if (selectedItem !in settings.list_of_widgets) {
                                    settings.list_of_widgets.add(selectedItem)
                                    settings.number_of_widgets++
                                    settings.saveSettings()
                                    squareListState.value = settings.list_of_widgets.toList()
                                }
                            }
                        )
                    }
                }

            }
        }
    }

    private fun convertHourToInteger(hourString: String): Int {
        // Extract the numeric part and the AM/PM part
        val regex = Regex("(\\d+)(AM|PM)")
        val match = regex.matchEntire(hourString.trim().uppercase())

        // Convert string into an integer 0-23
        if (match != null) {
            val (hour, period) = match.destructured
            val hourInt = hour.toInt()

            return when (period) {
                "AM" -> if (hourInt == 12) 0 else hourInt
                "PM" -> if (hourInt == 12) 12 else hourInt + 12
                else -> 0 // Default value
            }
        }
        else
        {
            return 0
        }
    }

    fun convertTo12HourFormat(time: String): String {
        // Split the input string into hour and minute
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1]

        // Determine AM or PM
        val amPm = if (hour < 12) "AM" else "PM"

        // Convert the hour to 12-hour format
        val hour12 = if (hour == 0) {
            12
        } else if (hour > 12) {
            hour - 12
        } else {
            hour
        }

        // Return the formatted time string
        return "$hour12:$minute$amPm"
    }
}