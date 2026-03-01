package io.github.keanu365.studbud

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "StudBud",
    ) {
        App()
    }
}