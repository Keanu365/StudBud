package io.github.keanu365.studbud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_error
import studbud.composeapp.generated.resources.icon_info
import studbud.composeapp.generated.resources.icon_wand
import studbud.composeapp.generated.resources.icon_warning

@Composable
fun SurfaceAlert(
    alertType: AlertType,
    message: String,
    modifier: Modifier = Modifier,
){
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ){
            Icon(
                painter = painterResource(
                    when(alertType){
                        AlertType.SUGGESTION -> Res.drawable.icon_wand
                        AlertType.INFO -> Res.drawable.icon_info
                        AlertType.WARNING -> Res.drawable.icon_warning
                        AlertType.ERROR -> Res.drawable.icon_error
                    }
                ),
                contentDescription = null,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 15.dp)
            )
        }
    }
}

enum class AlertType{
    SUGGESTION, INFO, WARNING, ERROR
}