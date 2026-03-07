package io.github.keanu365.studbud

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.keanu365.studbud.navigation.NavRoot
import io.github.keanu365.studbud.theme.StudBudTheme
import io.github.keanu365.studbud.theme.typography

@Composable
@Preview
fun App() {
//    ThemeTest()
//    TODO Actual project
    StudBudTheme {
        Scaffold { innerPadding ->
            NavRoot(
                modifier = Modifier
                    .padding(innerPadding)
            )
        }
    }
}