package io.github.keanu365.studbud

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mmk.kmpnotifier.extensions.composeDesktopResourcesPath
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import java.io.File

private lateinit var appPreferencesInstance: AppPreferences

fun main() = application {
    if (!::appPreferencesInstance.isInitialized) {
        val dataStore = createDataStore()
        appPreferencesInstance = AppPreferences(dataStore)
    }
    NotifierManager.initialize(
        NotificationPlatformConfiguration.Desktop(
            showPushNotification = true,
            notificationIconPath = composeDesktopResourcesPath() + File.separator + "ic_notification.png"
        )
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "StudBud",
    ) {
        App(appPreferencesInstance)
    }
}