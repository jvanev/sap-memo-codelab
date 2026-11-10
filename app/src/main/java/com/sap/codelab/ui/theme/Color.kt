package com.sap.codelab.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkGreen = Color(0xFF528600)
val DarkRed = Color(0xFFCB1101)
val LightGreen = Color(0xFF9CCC65)
val Orange = Color(0xFFF8703F)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

@Composable
fun ButtonDefaults.sapTopAppBarTextButtonColors(): ButtonColors = textButtonColors(
    contentColor = Color.White,
)
