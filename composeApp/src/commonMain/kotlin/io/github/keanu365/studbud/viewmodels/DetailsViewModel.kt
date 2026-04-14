package io.github.keanu365.studbud.viewmodels

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import io.github.keanu365.studbud.ErrorButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_warning

open class DetailsViewModel: ViewModel() {
    protected val _alert = MutableStateFlow<@Composable () -> Unit>({})
    val alert = _alert.asStateFlow()
    @Composable
    protected fun Alert(
        title: String,
        text: String,
        onConfirm: () -> Unit,
    ){
        AlertDialog(
            onDismissRequest = {
                _alert.value = {}
            },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.icon_warning),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                ErrorButton(
                    onClick = {
                        _alert.value = {}
                        onConfirm()
                    }
                ){
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        _alert.value = {}
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ){
                    Text("No")
                }
            }
        )
    }

}