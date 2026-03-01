package io.github.keanu365.studbud.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import studbud.composeapp.generated.resources.*

@Composable
fun poppins() = FontFamily(
    Font(Res.font.poppins_black, FontWeight.Black),
    Font(Res.font.poppins_black_italic, FontWeight.Black, FontStyle.Italic),
    Font(Res.font.poppins_bold, FontWeight.Bold),
    Font(Res.font.poppins_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(Res.font.poppins_extra_bold, FontWeight.ExtraBold),
    Font(Res.font.poppins_extra_bold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(Res.font.poppins_extra_light, FontWeight.ExtraLight),
    Font(Res.font.poppins_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.poppins_light, FontWeight.Light),
    Font(Res.font.poppins_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.poppins_medium, FontWeight.Medium),
    Font(Res.font.poppins_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.poppins_regular, FontWeight.Normal),
    Font(Res.font.poppins_semi_bold, FontWeight.SemiBold),
    Font(Res.font.poppins_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.poppins_thin, FontWeight.Thin),
    Font(Res.font.poppins_thin_italic, FontWeight.Thin, FontStyle.Italic),
)

@Composable
fun typography() = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = poppins()),
        displayMedium = displayMedium.copy(fontFamily = poppins()),
        displaySmall = displaySmall.copy(fontFamily = poppins()),
        headlineLarge = headlineLarge.copy(fontFamily = poppins()),
        headlineMedium = headlineMedium.copy(fontFamily = poppins()),
        headlineSmall = headlineSmall.copy(fontFamily = poppins()),
        titleLarge = titleLarge.copy(fontFamily = poppins()),
        titleMedium = titleMedium.copy(fontFamily = poppins()),
        titleSmall = titleSmall.copy(fontFamily = poppins()),
        bodyLarge = bodyLarge.copy(fontFamily = poppins()),
        bodyMedium = bodyMedium.copy(fontFamily = poppins()),
        bodySmall = bodySmall.copy(fontFamily = poppins()),
        labelLarge = labelLarge.copy(fontFamily = poppins()),
        labelMedium = labelMedium.copy(fontFamily = poppins()),
        labelSmall = labelSmall.copy(fontFamily = poppins())
    )
}