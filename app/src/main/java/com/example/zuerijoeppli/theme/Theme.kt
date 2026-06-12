package com.example.zuerijoeppli.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EcoGreenDark,
    secondary = ZurichBlueDark,
    background = LightGreyDark,
    surface = SurfaceDark,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = LightGrey,
    onSurface = LightGrey
)

private val LightColorScheme = lightColorScheme(
    primary = EcoGreen,
    secondary = ZurichBlue,
    background = LightGrey,
    surface = PureWhite,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = DarkSlate,
    onSurface = DarkSlate
)

@Composable
fun ZueriJoeppliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
