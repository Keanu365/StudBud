package io.github.keanu365.studbud

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.keanu365.studbud.navigation.NavRoot
import io.github.keanu365.studbud.theme.StudBudTheme

val supabase = createSupabaseClient(
    supabaseUrl = "https://dyikkrnyteudomofjrdz.supabase.co",
    supabaseKey = "sb_publishable_JqT90Z2mK6aOa7sFsz9PpQ_7OMuWspd"
) {
    install(Postgrest)
}

@Composable
@Preview
fun App() {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                    }
            )
        }
    }
}