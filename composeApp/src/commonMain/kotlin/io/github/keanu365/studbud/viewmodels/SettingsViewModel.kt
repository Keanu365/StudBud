package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.keanu365.studbud.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SettingsViewModel(
    private val appPrefs: AppPreferences
) : ViewModel() {
    private val _settings = MutableStateFlow(Settings())
    val settings = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            // collectLatest cancels previous processing if a new value arrives quickly
            appPrefs.settings.collectLatest { jsonString ->
                if (jsonString.isNotEmpty()) {
                    try {
                        val decodedSettings = Json.decodeFromString<Settings>(jsonString)
                        _settings.update { decodedSettings }
                    } catch (_: Exception) {
                        // Log error if needed: println("Failed to decode settings: ${e.message}")
                        _settings.update { Settings(theme = Theme.SYSTEM) }
                    }
                }
            }
        }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            val newSettings = Settings(theme = theme)
            // 1. Update UI state immediately
            _settings.update { newSettings }

            // 2. Persist to DataStore as JSON
            try {
                appPrefs.setSettings(newSettings)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@Serializable
data class Settings(
    val theme: Theme = Theme.SYSTEM,
)

@Serializable
enum class Theme(val displayName: String) {
    @SerialName("LIGHT") LIGHT("Light"),
    @SerialName("DARK") DARK("Dark"),
    @SerialName("SYSTEM") SYSTEM("System"),
    @SerialName("TIME") TIME("Time");

    companion object {
        fun getDisplayNames(): List<String> = entries.map { it.displayName }

        fun getTheme(displayName: String): Theme =
            entries.find { it.displayName.equals(displayName, ignoreCase = true) } ?: SYSTEM
    }
}