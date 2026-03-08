package io.github.keanu365.studbud.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.keanu365.studbud.theme.buttonColors
import io.github.keanu365.studbud.theme.outlinedTextFieldColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.*

@Composable
fun SignUpPage(
    modifier: Modifier = Modifier
){
    val snackBarHostState = remember { SnackbarHostState() }
    val signUpScope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
            )
    ){
        Spacer(
            modifier = Modifier.height(5.dp)
        )
        Text(
            text = "Sign up to get started!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 15.dp)
                .focusable()
        )

        var email by remember {mutableStateOf("")}
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember {mutableStateOf("")}

        var isEmailError by remember {mutableStateOf(false)}
        var isUsernameError by remember {mutableStateOf(false)}
        var isPasswordError by remember {mutableStateOf(false)}
        var doesPasswordConflict by remember {mutableStateOf(false)}

        InfoField(
            value = email,
            onValueChange = {
                email = it
                isEmailError = false
            },
            labelText = "Email",
            leadingIconResource = Res.drawable.icon_email,
            isError = isEmailError
        )
        InfoField(
            value = username,
            onValueChange = {
                username = it
                isUsernameError = false
            },
            labelText = "Username",
            leadingIconResource = Res.drawable.icon_user,
            isError = isUsernameError
        )
        InfoField(
            value = password,
            onValueChange = {
                password = it
                isPasswordError = false
            },
            labelText = "Password",
            leadingIconResource = Res.drawable.icon_lock,
            isError = isPasswordError,
            isPassword = true
        )
        InfoField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                doesPasswordConflict = it != password
            },
            labelText = "Confirm Password",
            leadingIconResource = Res.drawable.icon_lock,
            isError = doesPasswordConflict,
            isPassword = true
        )

        Button(
            onClick = {
                isEmailError = !isEmailValid(email)
                isUsernameError = username.isBlank()
                isPasswordError = password.isBlank()
                doesPasswordConflict = password != confirmPassword
                if (isEmailError || isUsernameError || isPasswordError || doesPasswordConflict) {
                    signUpScope.launch { snackBarHostState.showSnackbar("Sign up failed.") }
                } else {
                    //TODO: Sign up logic and individual error messages
                    signUpScope.launch { snackBarHostState.showSnackbar("Sign up successful!") }
                }
            },
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .height(50.dp),
            colors = buttonColors()
        ){
            Text(
                text = "SIGN UP",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ){
            Text(
                text = "Already have an account?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "SIGN IN",
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable{
                        //TODO
                    }
            )
        }
        SnackbarHost(
            hostState = snackBarHostState
        )
    }
}

@Composable
private fun InfoField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    leadingIconResource: DrawableResource,
    isError: Boolean,
    isPassword: Boolean = false
){
    var showPassword by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(labelText) },
        leadingIcon = {
            Icon(
                painter = painterResource(leadingIconResource),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
            )
        },
        singleLine = true,
        isError = isError,
        colors = outlinedTextFieldColors(),
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
}