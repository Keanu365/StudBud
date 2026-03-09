package io.github.keanu365.studbud.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.keanu365.studbud.theme.outlinedTextFieldColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_error
import studbud.composeapp.generated.resources.icon_invisible
import studbud.composeapp.generated.resources.icon_visible

@Composable
fun InfoField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    leadingIconResource: DrawableResource? = null,
    isError: Boolean,
    errorText: String = "Error",
    isPassword: Boolean = false
){
    var showPassword by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()){
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            label = { Text(labelText) },
            leadingIcon = {
                leadingIconResource?.let{
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
            },
            singleLine = true,
            isError = isError,
            colors = outlinedTextFieldColors(),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .fillMaxWidth()
                .height(75.dp),
            //Password transformations
            trailingIcon =  {
                if (isPassword)
                    IconButton(
                        onClick = { showPassword = !showPassword },
                    ){
                        Icon(
                            painter = painterResource(
                                if (showPassword) Res.drawable.icon_visible
                                else Res.drawable.icon_invisible
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
            },
            visualTransformation = if (showPassword || !isPassword) VisualTransformation.None else PasswordVisualTransformation(),
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 15.dp, vertical = 5.dp)
        ) {
            if (isError) {
                Icon(
                    painter = painterResource(Res.drawable.icon_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}