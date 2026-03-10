package io.github.keanu365.studbud

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import io.github.keanu365.studbud.navigation.NavRoot
import io.github.keanu365.studbud.theme.StudBudTheme

@Composable
@Preview
fun App() {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dataStore = remember { createDataStore() }
    val appPrefs = remember { AppPreferences(dataStore) }

    //TODO Add a SnackBar here to act as a "Toast" in-app
    StudBudTheme {
        Scaffold { innerPadding ->
            NavRoot(
                modifier = Modifier
                    .padding(innerPadding)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
                appPrefs = appPrefs
            )
        }
    }
}