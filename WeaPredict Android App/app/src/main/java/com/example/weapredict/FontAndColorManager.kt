package com.example.weapredict

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object FontAndColorManager {
    private val lexendDecaFamily = FontFamily(
        Font(R.font.lexenddeca_black, FontWeight.Black),
        Font(R.font.lexenddeca_bold, FontWeight.Bold),
        Font(R.font.lexenddeca_extrabold, FontWeight.ExtraBold),
        Font(R.font.lexenddeca_light, FontWeight.Light),
        Font(R.font.lexenddeca_medium, FontWeight.Medium),
        Font(R.font.lexenddeca_regular, FontWeight.Normal),
        Font(R.font.lexenddeca_semibold, FontWeight.SemiBold),
        Font(R.font.lexenddeca_thin, FontWeight.Thin)
    )

    private val syncopateFamily = FontFamily(
        Font(R.font.syncopate_bold, FontWeight.Bold),
        Font(R.font.syncopate_regular, FontWeight.Normal)
    )

    public fun getLexendDeca() : FontFamily
    {
        return lexendDecaFamily
    }

    public fun getSyncopate() : FontFamily
    {
        return syncopateFamily
    }

    public var backgroundColor = Color(0xFFFFFFFF) // White
    public var foregroundColor = Color(0xFF00AAEB) // Light Blue
    public var majorTextColor = Color(0xFF000000) // Black
    public var minorTextColor = Color(0xFFFFFFFF) // White

    public fun refreshColorPalette(
        weatherData : WeatherManager.WeatherInstance,
        additionalData : WeatherManager.AdditionalDataInstance
    )
    {
        var weatherString = weatherData.weather_type
        val isDaytime = additionalData.isDay

        // Collect the color palette
        val colorMap = mapOf(
            "Clear sky" to ColorSet(
                background = if (isDaytime) Color(0xFFFFFFFF) else Color(0xFF2C2C2C),
                foreground = if (isDaytime) Color(0xFF00AAEB) else Color(0xFF2C3E50),
                majorText = if (isDaytime) Color(0xFF000000) else Color(0xFFFFFFFF),
                minorText = if (isDaytime) Color(0xFF000000) else Color(0xFFFFFFFF)
            ),
            "Partly cloudy" to ColorSet(
                background = if (isDaytime) Color(0xFFFFFFFF) else Color(0xFF2C2C2C),
                foreground = if (isDaytime) Color(0xFF00AAEB) else Color(0xFF2C3E50),
                majorText = if (isDaytime) Color(0xFF000000) else Color(0xFFFFFFFF),
                minorText = if (isDaytime) Color(0xFF000000) else Color(0xFFFFFFFF)
            ),
            "Foggy" to ColorSet(
                background = if (isDaytime) Color(0xFFE0E0E0) else Color(0xFF1C1C1C),
                foreground = if (isDaytime) Color(0xFF7F8C8D) else Color(0xFF34495E),
                majorText = if (isDaytime) Color(0xFF34495E) else Color(0xFFBDC3C7),
                minorText = if (isDaytime) Color(0xFF34495E) else Color(0xFFBDC3C7),
            ),
            "Drizzle" to ColorSet(
                background = Color(0xFFE6F2FF),
                foreground = Color(0xFF5DADE2),
                majorText = Color(0xFF2C3E50),
                minorText = Color(0xFF34495E)
            ),
            "Rain showers" to ColorSet(
                background = Color(0xFFC6E2FF),
                foreground = Color(0xFF2980B9),
                majorText = Color(0xFF34495E),
                minorText = Color(0xFF2980B9)
            ),
            "Rain" to ColorSet(
                background = Color(0xFFC6E2FF),
                foreground = Color(0xFF2980B9),
                majorText = Color(0xFF34495E),
                minorText = Color(0xFFFFFFFF)
            ),
            "Snow" to ColorSet(
                background = Color(0xFFF0F8FF),
                foreground = Color(0xFFBDC3C7),
                majorText = Color(0xFF2C3E50),
                minorText = Color(0xFFFFFFFF)
            ),
            "Snow showers" to ColorSet(
                background = Color(0xFFF0F8FF),
                foreground = Color(0xFFBDC3C7),
                majorText = Color(0xFF2C3E50),
                minorText = Color(0xFFFFFFFF)
            ),
            "Snow grains" to ColorSet(
                background = Color(0xFFF0F8FF),
                foreground = Color(0xFFBDC3C7),
                majorText = Color(0xFF2C3E50),
                minorText = Color(0xFFFFFFFF)
            ),
            "Thunderstorm" to ColorSet(
                background = Color(0xFF1A1A2E),
                foreground = Color(0xFF8E44AD),
                majorText = Color(0xFFECF0F1),
                minorText = Color(0xFFECF0F1)
            ),
            "Thunderstorm with hail" to ColorSet(
                background = Color(0xFF2C3E50),
                foreground = Color(0xFF7D3C98),
                majorText = Color(0xFFFFFFFF),
                minorText = Color(0xFFBDC3C7)
            )
        )

        // If a color palette was found, set the UI colors
        val colorSet = colorMap[weatherString] ?: ColorSet(
            background = Color(0xFFFFFFFF),
            foreground = Color(0xFF00AAEB),
            majorText = Color(0xFF000000),
            minorText = Color(0xFF000000)
        )

        backgroundColor = colorSet.background
        foregroundColor = colorSet.foreground
        majorTextColor = colorSet.majorText
        minorTextColor = colorSet.minorText
    }

    // Makes code easier to read
    data class ColorSet(
        val background: Color,
        val foreground: Color,
        val majorText: Color,
        val minorText: Color
    )
}