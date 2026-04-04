package io.github.keanu365.studbud

import androidx.compose.ui.window.ComposeUIViewController

private val appPrefs by lazy { AppPreferences(createDataStore()) }

fun MainViewController() = ComposeUIViewController {
    App(appPrefs)
}