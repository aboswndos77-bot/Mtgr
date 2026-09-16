package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val BinanceDarkColorScheme = darkColorScheme(
    primary = BinanceYellow,
    onPrimary = Color.Black,
    primaryContainer = BinanceCardDark,
    onPrimaryContainer = BinanceYellowLight,
    secondary = BinanceYellowLight,
    onSecondary = Color.Black,
    tertiary = BinanceGreen,
    onTertiary = Color.Black,
    error = BinanceRed,
    onError = Color.White,
    background = BinanceBgDark,
    onBackground = BinanceTextWhite,
    surface = BinanceCardDark,
    onSurface = BinanceTextWhite,
    surfaceVariant = BinanceInputDark,
    onSurfaceVariant = BinanceTextGray,
    outline = BinanceBorderDark
)

// Binance Light scheme (Clean high-contrast Binance light)
private val BinanceLightColorScheme = lightColorScheme(
    primary = BinanceYellowDark,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFFFEF9C3),
    onPrimaryContainer = Color(0xFF854D0E),
    secondary = BinanceYellow,
    onSecondary = Color.Black,
    tertiary = BinanceGreen,
    onTertiary = Color.White,
    error = BinanceRed,
    onError = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF181A20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF181A20),
    surfaceVariant = Color(0xFFF0F3F6),
    onSurfaceVariant = Color(0xFF707A8A),
    outline = Color(0xFFE6E8EA)
)

@Composable
fun YemeniStoreTheme(
    darkTheme: Boolean = true, // Binance iconic default dark experience
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BinanceDarkColorScheme else BinanceLightColorScheme

    // Arabic RTL Layout direction enforcement
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

