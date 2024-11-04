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
    // Rebuild both lists with the latest information retrieved from models
    fun refreshWeatherPredictions(
        currentWeatherData: WeatherManager.WeatherInstance,
        dailyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>,
        hourlyWeatherDataList: SnapshotStateList<WeatherManager.WeatherInstance>
    ){
        // TODO: Rebuild both lists with the latest information retrieved from models
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

    fun predictTemperature(modelInput: Date, temperatureModel: Interpreter): Array<FloatArray> {
        // Convert Date into an array of floats
        val calendar = Calendar.getInstance().apply { time = modelInput }
        val timeFeatures = Array(1) { FloatArray(4) }.apply {
            this[0][0] = calendar.get(Calendar.HOUR_OF_DAY).toFloat() / 24f
            this[0][1] = calendar.get(Calendar.DAY_OF_MONTH).toFloat() / 31f
            this[0][2] = (calendar.get(Calendar.MONTH) + 1).toFloat() / 12f
            this[0][3] = calendar.get(Calendar.YEAR).toFloat() / 2024f // May need to change
        }
        // Run model and return output
        val temperatureModelTestOutput = Array(1) { FloatArray(1) }
        temperatureModel.run(timeFeatures, temperatureModelTestOutput)
        return temperatureModelTestOutput
    }

    fun predictWeatherClass(modelInput: Float, weatherModel: Interpreter): String {
        // Shapes and fills the input data
        val inputShape = weatherModel.getInputTensor(0).shape()
        val weatherModelInput = Array(inputShape[0]) { FloatArray(inputShape[1])}
        weatherModelInput[0][0] = modelInput
        // Shapes and initializes the output data array
        val outputShape = weatherModel.getOutputTensor(0).shape()
        val weatherModelOutput = Array(outputShape[0]) { FloatArray(outputShape[1]) }

        weatherModel.run(modelInput, weatherModelOutput)

        // Picks the most likely weather classification
        var maxIndex = 0
        var maxValue = weatherModelOutput[0][0]
        for (i in 0 until weatherModelOutput[0].size) {
            if (weatherModelOutput[0][i] > maxValue) {
                maxValue = weatherModelOutput[0][i]
                maxIndex = i
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

        val predictedWeather = mapping[maxIndex] ?: "Unknown Weather"

        return predictedWeather
    }
}