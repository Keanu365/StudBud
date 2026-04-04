package io.github.keanu365.studbud

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

private lateinit var appPreferencesInstance: AppPreferences

fun main() = application {
    if (!::appPreferencesInstance.isInitialized) {
        val dataStore = createDataStore()
        appPreferencesInstance = AppPreferences(dataStore)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "StudBud",
    ) {
        App(appPreferencesInstance)
    }
}