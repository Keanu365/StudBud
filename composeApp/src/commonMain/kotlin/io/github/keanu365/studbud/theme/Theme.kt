package io.github.keanu365.studbud.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PriGreenDark,
    secondary = LimeGreenDark,
    tertiary = ButtonBlueDark,
    error = ErrorRedDark,
    background = BgBlack,
    surface = SurfaceBlack,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onError = Color.White,
    onBackground = SurfaceGrey,
    onSurface = SurfaceGrey,
    onSurfaceVariant = SecondText
)

private val LightColorScheme = lightColorScheme(
    primary = PriGreen,
    secondary = LimeGreen,
    tertiary = ButtonBlue,
    error = ErrorRed,
    background = BgGrey,
    surface = SurfaceGrey,

    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = BgGrey,
    onError = BgGrey,
    onBackground = Color.Black,
    onSurface = BgBlack,
    onSurfaceVariant = SecondText
)

@Composable
fun StudBudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography(),
        content = content
    )
}
