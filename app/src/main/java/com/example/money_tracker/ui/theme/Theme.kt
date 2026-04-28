package com.example.money_tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple200,
    secondary = Mint200,
    tertiary = Pink200,
    
    background = Color(0xFFF3EDFF),
    surface = Color(0xFFEDE7F6),

    onPrimary = SoftBlack,
    onSecondary = SoftBlack,
    onTertiary = SoftBlack,
    onBackground = SoftBlack,
    onSurface = SoftBlack
)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    secondary = Mint500,
    tertiary = Pink500,

    background = SoftWhite,
    surface = SoftWhite,

    onPrimary = Color.White,
    onSecondary = SoftBlack,
    onTertiary = SoftBlack,
    onBackground = SoftBlack,
    onSurface = SoftBlack
)

@Composable
fun Money_TrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}