package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.AppPreferences
import io.github.keanu365.studbud.TitleText
import io.github.keanu365.studbud.viewmodels.SettingsViewModel
import io.github.keanu365.studbud.viewmodels.Theme
import kotlinx.coroutines.launch

@Composable
fun SettingsPage(
    appPrefs: AppPreferences,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel(appPrefs) }
){
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val settingsScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
            .verticalScroll(rememberScrollState())
    ){
        Spacer(Modifier.height(20.dp))
        TitleText("Settings")
        // UI Theme
        SettingsItem("App Theme"){
            RadioColumn(
                onOptionSelected = { option ->
                    settingsScope.launch {
                        viewModel.setTheme(Theme.getTheme(option))
                    }
                },
                options = Theme.getDisplayNames(),
                currentOption = settings.theme.displayName
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    content: @Composable ColumnScope.() -> Unit
){
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
        )
        content()
    }
}

@Composable
private fun RadioColumn(
    onOptionSelected: (String) -> Unit,
    options: List<String>,
    currentOption: String = options[0]
){
    var selectedOption by remember { mutableStateOf(currentOption) }
    Column(modifier = Modifier.selectableGroup()){
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            selectedOption = option
                            onOptionSelected(option)
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}