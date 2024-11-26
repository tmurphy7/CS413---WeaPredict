package com.example.weapredict

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.stream.IntStream.range
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object UserInterfaceManager {

    @Composable
    fun DisplayDays(dailyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>){
        //Display the weather for 7 day forecast
        val lexendDecaFont = FontAndColorManager.getLexendDeca()
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
                        color = FontAndColorManager.minorTextColor,
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
                        color = FontAndColorManager.minorTextColor,
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
        val lexendDecaFont = FontAndColorManager.getLexendDeca()

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
                            color = FontAndColorManager.minorTextColor,
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
                            color = FontAndColorManager.minorTextColor,
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
                            color = FontAndColorManager.minorTextColor,
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
                            color = FontAndColorManager.minorTextColor,
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
                            color = FontAndColorManager.minorTextColor,
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
                            color = FontAndColorManager.minorTextColor,
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
    fun RefreshButton(onClick: () -> Unit) {
        Button(onClick = { onClick() }) {
            Text("Refresh Data")
        }
    }

    @Composable
    fun CustomWeatherSquares(settings: Settings){
        //Display the weather for 7 day forecast
        var expanded by remember { mutableStateOf(false) }
        var selectedItem by remember { mutableStateOf("") }

        val squareListState = remember { mutableStateOf(settings.list_of_widgets.toList()) }

        Row(modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth()) {

            //able to have more  to display
            settings.loadSettings()
            val num_squares = settings.number_of_widgets
            val square_list = settings.list_of_widgets


            for(square_title in squareListState.value){
                   Card(
                        modifier = Modifier
                            .size(width = 210.dp, height = 190.dp)
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .padding(horizontal = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FontAndColorManager.foregroundColor
                        )
                    ){
                        Text(
<<<<<<< HEAD
                            text = square_title,
=======
                            text = square_list[squares],
                            color = FontAndColorManager.minorTextColor,
                            fontFamily = FontAndColorManager.getLexendDeca(),
>>>>>>> e475bb2c8ebf258647bcedd5a49af10c81ea98c4
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
            }
            Card(
                modifier = Modifier.clickable { expanded = true }
                    .size(width = 210.dp, height = 190.dp)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .padding(horizontal = 5.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FontAndColorManager.foregroundColor
                )
            ) {
                Text(
                    text = "ADD",
                    color = FontAndColorManager.minorTextColor,
                    fontFamily = FontAndColorManager.getLexendDeca(),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = {expanded = false})
                {
                    DropdownMenuItem(
                        text = {Text("Humidity")},
                        onClick = {
                            expanded = false
                            selectedItem = "Humidity"
                            if(selectedItem !in settings.list_of_widgets){
                                settings.list_of_widgets.add(selectedItem)
                                settings.number_of_widgets++
                                settings.saveSettings()
                                squareListState.value = settings.list_of_widgets.toList()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = {Text("Rain Fall")},
                        onClick = {
                            expanded = false
                            selectedItem = "Rain Fall"
                            if(selectedItem !in settings.list_of_widgets){
                                settings.list_of_widgets.add(selectedItem)
                                settings.number_of_widgets++
                                settings.saveSettings()
                                squareListState.value = settings.list_of_widgets.toList()
                            }

                        }
                    )
                    DropdownMenuItem(
                        text = {Text("Snow Fall")},
                        onClick = {
                            expanded = false
                            selectedItem = "Snow Fall"
                            if(selectedItem !in settings.list_of_widgets){
                                settings.list_of_widgets.add(selectedItem)
                                settings.number_of_widgets++
                                settings.saveSettings()
                                squareListState.value = settings.list_of_widgets.toList()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = {Text("Wind Speed")},
                        onClick = {
                            expanded = false
                            selectedItem = "Wind Speed"
                            if(selectedItem !in settings.list_of_widgets){
                                settings.list_of_widgets.add(selectedItem)
                                settings.number_of_widgets++
                                settings.saveSettings()
                                squareListState.value = settings.list_of_widgets.toList()
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = {Text("UV Index")},
                        onClick = {
                            expanded = false
                            selectedItem = "UV Index"
                            if(selectedItem !in settings.list_of_widgets){
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