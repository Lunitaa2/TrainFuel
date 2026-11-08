package com.trainfuel.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TfYellow = Color(0xFFFFD60A)
val TfBlack = Color(0xFF0A0A0A)
val TfSurface = Color(0xFF151515)
val TfSurface2 = Color(0xFF212121)
val TfOutline = Color(0xFF333333)
val TfMuted = Color(0xFF9B9B9B)
val TfRed = Color(0xFFFF5A4F)

private val TrainFuelColors = darkColorScheme(
    primary = TfYellow,
    onPrimary = TfBlack,
    secondary = TfYellow,
    onSecondary = TfBlack,
    background = TfBlack,
    onBackground = Color.White,
    surface = TfSurface,
    onSurface = Color.White,
    surfaceVariant = TfSurface2,
    onSurfaceVariant = TfMuted,
    outline = TfOutline,
    primaryContainer = TfYellow,
    onPrimaryContainer = TfBlack
)

@Composable
fun TrainFuelTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TrainFuelColors, content = content)
}
