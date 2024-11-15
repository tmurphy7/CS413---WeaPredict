package com.example.weapredict

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Calendar
import java.util.Date

object ModelManager {

    private var temperatureModelName = "Temperature.tflite"
    private lateinit var temperatureModel: Interpreter
    private var weatherModelName = "weatherClass.tflite"
    private lateinit var weatherModel: Interpreter

    // Rebuild both lists with the latest information retrieved from models
    fun refreshWeatherPredictions(
        context: Context,
        currentWeatherData: WeatherManager.WeatherInstance,
        dailyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>,
        hourlyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>
    ){
        // TODO: ALL needs to be reviewed by T and R to make sure everything's input and output correctly
        // Utilize temperature model to get temperature forecast
        val currentDateTime = Date()
        temperatureModel = loadModelFromAssetsFolder(temperatureModelName, context)
        val hourlyTempOutput = predictHourlyTemperature(currentDateTime, temperatureModel)
        val dailyTempOutput = predictDailyTemperature(currentDateTime, temperatureModel)

        // Convert FloatArray to Array<FloatArray>
        val hourlyWeatherModelInput = Array(24) { FloatArray(1) }
        val dailyWeatherModelInput = Array(7) { FloatArray(1) }
        for (hour in 0 until 24) { hourlyWeatherModelInput[hour][0] = hourlyTempOutput[hour] }
        for (day in 0 until 7) { dailyWeatherModelInput[day][0] = dailyTempOutput[day] }

        // Utilize weather model to get weather forecast using temperature (and other inputs as needed)
        // TODO: Add more inputs for weather model (?)
        weatherModel = loadModelFromAssetsFolder(weatherModelName, context)
        val hourlyWeatherModelOutput = predictWeatherClass(hourlyWeatherModelInput, weatherModel)
        val dailyWeatherModelOutput = predictWeatherClass(dailyWeatherModelInput, weatherModel)

        // Convert outputs into weather objects
        // TODO: May need to add day / time / hour
        hourlyWeatherDataList[0] = currentWeatherData
        for (hour in 1 until 24) {
            hourlyWeatherDataList[hour] = WeatherManager.WeatherInstance(
                weather_type = hourlyWeatherModelOutput[hour],
                temperature_high = hourlyTempOutput[hour].toDouble(),
                temperature_low = hourlyTempOutput[hour].toDouble()
            )
        }
        for (day in 0 until 7) {
            dailyWeatherDataList[day] = WeatherManager.WeatherInstance(
                weather_type = dailyWeatherModelOutput[day],
                temperature_high = dailyTempOutput[day].toDouble(),
                temperature_low = dailyTempOutput[day].toDouble()
            )
        }

        // Debug code
        // for (hour in 0 until 24) { Log.d("DEBUG", "Type: ${hourlyWeatherDataList[hour].weather_type}") }
    }

    // "modelPath" should be a string like "model.tflite", context should just be 'this' when called from MainActivity
    fun loadModelFromAssetsFolder(modelPath: String, context: Context): Interpreter {
        try {
            val tfLiteModel = loadModelFile(modelPath, context)
            val options = Interpreter.Options()
            return Interpreter(tfLiteModel, options)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error loading model: ${e.message}")
        }
    }

    private fun loadModelFile(modelPath: String, context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictHourlyTemperature(modelInput: Date, temperatureModel: Interpreter): FloatArray {
        // Convert Date into an array of floats
        val calendar = Calendar.getInstance().apply { time = modelInput }

        val predictions = FloatArray(24)
        for (hour in 0 until 24) {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            val timeFeatures = Array(1) { FloatArray(4) }.apply {
                this[0][0] = calendar.get(Calendar.HOUR_OF_DAY).toFloat() / 24f
                this[0][1] = calendar.get(Calendar.DAY_OF_MONTH).toFloat() / 31f
                this[0][2] = (calendar.get(Calendar.MONTH) + 1).toFloat() / 12f
                this[0][3] = calendar.get(Calendar.YEAR).toFloat() / 2024f // May need to change
            }
            // Run model and return output
            val output = Array(1) { FloatArray(1) }
            temperatureModel.run(timeFeatures, output)
            predictions[hour] = output[0][0]
        }

        return predictions
    }

    fun predictDailyTemperature(modelInput: Date, temperatureModel: Interpreter): FloatArray {
        // Convert Date into an array of floats
        val calendar = Calendar.getInstance().apply { time = modelInput }

        val predictions = FloatArray(7)
        for (day in 0 until 7) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val timeFeatures = Array(1) { FloatArray(4) }.apply {
                this[0][0] = calendar.get(Calendar.HOUR_OF_DAY).toFloat() / 24f
                this[0][1] = calendar.get(Calendar.DAY_OF_MONTH).toFloat() / 31f
                this[0][2] = (calendar.get(Calendar.MONTH) + 1).toFloat() / 12f
                this[0][3] = calendar.get(Calendar.YEAR).toFloat() / 2024f // May need to change
            }
            // Run model and return output
            val output = Array(1) { FloatArray(1) }
            temperatureModel.run(timeFeatures, output)
            predictions[day] = output[0][0]
        }

        return predictions
    }

    fun predictWeatherClass(modelInputs: Array<FloatArray>, weatherModel: Interpreter): Array<String> {
        // Shapes and initializes the output data array
        val outputShape = weatherModel.getOutputTensor(0).shape()
        val weatherModelOutput = Array(modelInputs.size) { FloatArray(outputShape[1]) } // Adjust to match input size

        weatherModel.run(modelInputs, weatherModelOutput) // Run the model for the current input

        // Prepare to collect predicted weather classifications
        val predictions = Array(modelInputs.size) { "Unknown Weather" }

        // Picks the most likely weather classification for each output
        for (i in weatherModelOutput.indices) {
            var maxIndex = 0
            var maxValue = weatherModelOutput[i][0]

            for (j in 0 until weatherModelOutput[i].size) {
                if (weatherModelOutput[i][j] > maxValue) {
                    maxValue = weatherModelOutput[i][j]
                    maxIndex = j
                }
            }

            // This mapping will need updating with every new weather model
            val mapping = mapOf(
                0 to "Clear Sky",       // 0.0
                1 to "Partly cloudy",   // 1.0
                2 to "Partly cloudy",   // 2.0
                3 to "Partly cloudy",   // 3.0
                4 to "Foggy",           // 45.0
                5 to "Drizzle",         // 51.0
                6 to "Drizzle",         // 53.0
                7 to "Drizzle",         // 55.0
                8 to "Drizzle",         // 56.0
                9 to "Drizzle",         // 57.0
                10 to "Rain",           // 61.0
                11 to "Rain",           // 63.0
                12 to "Rain",           // 65.0
                13 to "Rain",           // 66.0
                14 to "Rain",           // 67.0
                15 to "Rain showers",   // 80.0
                16 to "Rain showers",   // 81.0
                17 to "Thunderstorm"    // 95.0
            )

            predictions[i] = mapping[maxIndex] ?: "Unknown Weather"
        }

        return predictions
    }
}