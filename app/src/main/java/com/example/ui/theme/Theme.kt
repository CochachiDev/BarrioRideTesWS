package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EcoGreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = EcoGreenOnContainer,
    onPrimaryContainer = EcoGreenContainer,
    secondary = AmberAccent,
    background = SlateDark,
    surface = SlateMedium,
    onBackground = Color.White,
    onSurface = Color.White
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EcoGreenDark,
    onPrimary = Color.White,
    primaryContainer = EcoGreenContainer,
    onPrimaryContainer = EcoGreenOnContainer,
    secondary = AmberAccent,
    background = SurfaceLight,
    surface = CardSurface,
    onBackground = SlateDark,
    onSurface = SlateDark
  )

@Composable
fun BarrioRideTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use customized brand theme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
