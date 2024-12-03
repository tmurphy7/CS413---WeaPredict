package com.example.weapredict

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Calendar
import java.util.Date

import java.io.InputStream
import org.json.JSONArray


object ModelManager {

    private var temperatureModelName = "temp.tflite"
    private lateinit var temperatureModel: Interpreter
    private var weatherModelName = "weatherClass.tflite"
    private lateinit var weatherModel: Interpreter

    // Rebuild both lists with the latest information retrieved from models
    fun refreshWeatherPredictions(
        context: Context,
        currentWeatherData: WeatherManager.WeatherInstance,
        dailyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>,
        hourlyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>
    ) {
        // Set the start time to the next hour if needed
        val currentDateTime = Date()
        val calendar = Calendar.getInstance().apply { time = currentDateTime }
        if (calendar.get(Calendar.MINUTE) > 0) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1)
            calendar.set(Calendar.MINUTE, 0)
        }
        val startTime = calendar.time

        // Load the temperature model
        temperatureModel = loadModelFromAssetsFolder(temperatureModelName, context)

        // Generate hourly and daily temperature predictions
        val hourlyTempOutput = predictHourlyTemperature(startTime, temperatureModel)
        val dailyTempOutput = predictDailyTemperature(startTime, temperatureModel)

        // Log predictions for debugging
        Log.d("ModelManager", "Hourly Temperature Predictions: ${hourlyTempOutput.joinToString()}")
        Log.d("ModelManager", "Daily Temperature Predictions: ${dailyTempOutput.joinToString()}")

        // Create inputs for weather classification model
        val hourlyWeatherModelInput = Array(24) { FloatArray(1) }
        val dailyWeatherModelInput = Array(7) { FloatArray(1) }

        for (hour in 0 until 24) { hourlyWeatherModelInput[hour][0] = hourlyTempOutput[hour] }
        for (day in 0 until 7) { dailyWeatherModelInput[day][0] = dailyTempOutput[day] }

        // Load the weather classification model
        weatherModel = loadModelFromAssetsFolder(weatherModelName, context)

        val weatherClassScaler = Scaler(context)

        // Generate weather class predictions
        val hourlyWeatherModelOutput = predictWeatherClass(hourlyWeatherModelInput, weatherModel, weatherClassScaler)
        val dailyWeatherModelOutput = predictWeatherClass(dailyWeatherModelInput, weatherModel, weatherClassScaler)

        // Log weather predictions for debugging
        Log.d("ModelManager", "Hourly Weather Predictions: ${hourlyWeatherModelOutput.joinToString()}")
        Log.d("ModelManager", "Daily Weather Predictions: ${dailyWeatherModelOutput.joinToString()}")

        // Fill hourly weather data
        hourlyWeatherDataList[0] = currentWeatherData
        for (hour in 1 until 24) {
            hourlyWeatherDataList[hour] = WeatherManager.WeatherInstance(
                weather_type = hourlyWeatherModelOutput[hour],
                temperature_high = hourlyTempOutput[hour].toDouble(),
                temperature_low = hourlyTempOutput[hour].toDouble()
            )
        }

        // Fill daily weather data
        for (day in 0 until 7) {
            dailyWeatherDataList[day] = WeatherManager.WeatherInstance(
                weather_type = dailyWeatherModelOutput[day],
                temperature_high = dailyTempOutput[day].toDouble(),
                temperature_low = dailyTempOutput[day].toDouble()
            )
        }
    }

    // Load the model from the assets folder
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

    // Load the model file into memory
    private fun loadModelFile(modelPath: String, context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Predict hourly temperature
    fun predictHourlyTemperature(startTime: Date, temperatureModel: Interpreter): FloatArray {
        val calendar = Calendar.getInstance().apply { time = startTime }

        val predictions = FloatArray(24)
        for (hour in 0 until 24) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + hour)
            val timeFeatures = Array(1) { FloatArray(4) }.apply {
                this[0][0] = calendar.get(Calendar.HOUR_OF_DAY).toFloat() / 24f
                this[0][1] = calendar.get(Calendar.DAY_OF_MONTH).toFloat() / 31f
                this[0][2] = (calendar.get(Calendar.MONTH) + 1).toFloat() / 12f
                this[0][3] = calendar.get(Calendar.YEAR).toFloat() / 2024f
            }

            val output = Array(1) { FloatArray(1) }
            temperatureModel.run(timeFeatures, output)
            predictions[hour] = output[0][0]
        }

        return predictions
    }

    // Predict daily temperature (min/max)
    fun predictDailyTemperature(startTime: Date, temperatureModel: Interpreter): FloatArray {
        val calendar = Calendar.getInstance().apply { time = startTime }

        val predictions = FloatArray(7)
        for (day in 0 until 7) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + day)
            val timeFeatures = Array(1) { FloatArray(4) }.apply {
                this[0][0] = 0f  // Placeholder value for hour, daily model
                this[0][1] = calendar.get(Calendar.DAY_OF_MONTH).toFloat() / 31f
                this[0][2] = (calendar.get(Calendar.MONTH) + 1).toFloat() / 12f
                this[0][3] = calendar.get(Calendar.YEAR).toFloat() / 2024f
            }

            val output = Array(1) { FloatArray(1) }
            temperatureModel.run(timeFeatures, output)
            predictions[day] = output[0][0]
        }

        return predictions
    }

    // Predict weather classification based on model inputs
    // Order of inputs for weather class model:
    // temperature (°F), relative_humidity (%), rain (inch)
    // snowfall (inch), cloud cover (%), visibility (ft)
    fun predictWeatherClass(modelInputs: Array<FloatArray>, weatherModel: Interpreter, scaler: Scaler): Array<String> {
        val outputShape = weatherModel.getOutputTensor(0).shape()
        val weatherModelOutput = Array(modelInputs.size) { FloatArray(outputShape[1]) }

        val scaledInputs = modelInputs.map { input ->
            scaler.applyScaler(input) // Apply the scaling (standardization) on each input array
        }.toTypedArray()

        weatherModel.run(scaledInputs, weatherModelOutput)

        val predictions = Array(modelInputs.size) { "Unknown Weather" }

        for (i in weatherModelOutput.indices) {
            var maxIndex = 0
            var maxValue = weatherModelOutput[i][0]

            for (j in 0 until weatherModelOutput[i].size) {
                if (weatherModelOutput[i][j] > maxValue) {
                    maxValue = weatherModelOutput[i][j]
                    maxIndex = j
                }
            }

            val mapping = mapOf(
                0 to "Clear Sky", 1 to "Partly cloudy", 2 to "Mostly cloudy",
                3 to "Cloudy", 4 to "Foggy", 5 to "Drizzle", 6 to "Rain",
                7 to "Snow", 8 to "Thunderstorm"
            )

            predictions[i] = mapping[maxIndex] ?: "Unknown Weather"
        }

        return predictions
    }
}

class Scaler(context: Context) {    // Very scuffed method to apply the scaler
    private var means: FloatArray
    private var scales: FloatArray

    init {
        val inputStream: InputStream = context.assets.open("scaler_params.json")
        val inputStreamReader = InputStreamReader(inputStream)
        val reader = BufferedReader(inputStreamReader)

        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        val jsonString = stringBuilder.toString()
        val jsonObject = JSONObject(jsonString)

        val meanArray: JSONArray = jsonObject.getJSONArray("mean")
        val scaleArray: JSONArray = jsonObject.getJSONArray("scale")

        means = FloatArray(meanArray.length())
        scales = FloatArray(scaleArray.length())

        for (i in 0 until meanArray.length()) {
            means[i] = meanArray.getDouble(i).toFloat()
            scales[i] = scaleArray.getDouble(i).toFloat()
        }
    }

    fun applyScaler(inputData: FloatArray): FloatArray {
        val scaledData = FloatArray(inputData.size)
        for (i in inputData.indices) {
            scaledData[i] = (inputData[i] - means[i]) / scales[i]
        }
        return scaledData
    }
}