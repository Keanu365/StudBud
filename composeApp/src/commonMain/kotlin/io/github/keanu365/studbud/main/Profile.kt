package io.github.keanu365.studbud.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_error

@Composable
fun Profile(
    onSignOut: () -> Unit
){
    var showAlert by remember {mutableStateOf(false)}
    if (showAlert){
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.icon_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(50.dp)
                )
            },
            title = {
                Text("Sign Out")
            },
            text = {
                Text("Are you sure you want to sign out?")
            },
            onDismissRequest = {showAlert = false},
            confirmButton = {
                Button(
                    onClick = {
                        onSignOut()
                        showAlert = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ){Text("Yes")}
            },
            dismissButton = {
                Button(
                    onClick = {showAlert = false},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ){Text("No")}
            }
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text("Profile")
        Button(
            onClick = {
                showAlert = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .fillMaxWidth()
        ){
            Text(
                text = "SIGN OUT",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}