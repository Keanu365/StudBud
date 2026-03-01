package io.github.keanu365.studbud

import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import io.github.keanu365.studbud.theme.StudBudTheme
import io.github.keanu365.studbud.theme.typography

@Composable
@Preview
fun App(typography: Typography = typography()) {
    StudBudTheme(
        typography = typography
    ){
        ThemeTest()
    }
    //TODO Actual project
}