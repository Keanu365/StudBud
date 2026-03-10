package io.github.keanu365.studbud.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.keanu365.studbud.theme.buttonColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_lock
import studbud.composeapp.generated.resources.icon_user

@Composable
fun SignInPage(
    modifier: Modifier = Modifier,
    onSignUpClicked: () -> Unit,
    onSignIn: (User) -> Unit = {}
){
    val snackBarHostState = remember { SnackbarHostState() }
    val signUpScope = rememberCoroutineScope()

    var emailOrUsername by remember {mutableStateOf("")}
    var password by remember { mutableStateOf("") }

    var isEmailOrUsernameError by remember {mutableStateOf(false)}
    var isPasswordError by remember {mutableStateOf(false)}
    var submitAttempted by remember {mutableStateOf(false)}
    var buttonEnabled by remember {mutableStateOf(true)}
    fun performValidationChecks(): Boolean = run {
        isEmailOrUsernameError = emailOrUsername.isBlank()
        isPasswordError = password.isBlank()
        !(isEmailOrUsernameError || isPasswordError)
    }

    LaunchedEffect(emailOrUsername, password){
        if (submitAttempted) {
            buttonEnabled = performValidationChecks()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
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
            text = "Welcome back!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 15.dp)
                .focusable()
        )
        InfoField(
            value = emailOrUsername,
            onValueChange = {
                emailOrUsername = it
            },
            labelText = "Email or Username",
            leadingIconResource = Res.drawable.icon_user,
            isError = isEmailOrUsernameError,
            errorText = "Invalid email"
        )
        InfoField(
            value = password,
            onValueChange = {
                password = it
            },
            labelText = "Password",
            leadingIconResource = Res.drawable.icon_lock,
            isError = isPasswordError,
            isPassword = true,
            errorText = "Please fill in a password!"
        )

        Button(
            onClick = {
                submitAttempted = true
                buttonEnabled = performValidationChecks()
                if (performValidationChecks()) {
                    signUpScope.launch {
                        try {
                            val user = signIn(emailOrUsername, password)
                            snackBarHostState.showSnackbar("Sign in successful!")
//                            delay(2000)
                            onSignIn(user)
                            //TODO Error messages for incorrect username/password
                        } catch (_: HttpRequestException){
                            snackBarHostState.showSnackbar("There was a network error. Please check your connection and try again.")
                        } catch (e: Exception) {
                            val errorMessage = e.message ?: "Unknown error"
                            val friendlyMsg = if (errorMessage.contains("No rows"))
                                "Wrong email / username / password."
                            else errorMessage
                            snackBarHostState.showSnackbar("Sign in failed: $errorMessage")
                        }
                    }
                }
            },
            enabled = buttonEnabled,
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .height(50.dp),
            colors = buttonColors()
        ){
            Text(
                text = "SIGN IN",
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
                text = "Don't have an account?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "SIGN UP",
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable{
                        onSignUpClicked()
                    }
            )
        }
        SnackbarHost(
            hostState = snackBarHostState
        )
    }
}