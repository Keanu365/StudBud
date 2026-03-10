package io.github.keanu365.studbud

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.keanu365.studbud.navigation.NavRoot
import io.github.keanu365.studbud.theme.StudBudTheme
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dataStore = remember { createDataStore() }
    val appPrefs = remember { AppPreferences(dataStore) }

    val snackBarHostState = remember { SnackbarHostState() }
    val mainAppScope = rememberCoroutineScope()

    StudBudTheme {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ){
                NavRoot(
                    modifier = Modifier
                        .padding(innerPadding)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                            }
                        },
                    appPrefs = appPrefs,
                    showSnackBar = { message ->
                        mainAppScope.launch {
                            snackBarHostState.showSnackbar(message)
                        }
                    }
                )
                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier.padding(bottom = 50.dp)
                )
            }
        }
    }
}