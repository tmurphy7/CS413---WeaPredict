package com.example.weapredict

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object FontManager {
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
}