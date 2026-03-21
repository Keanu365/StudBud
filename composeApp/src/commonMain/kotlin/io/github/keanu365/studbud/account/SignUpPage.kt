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
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.keanu365.studbud.InfoField
import io.github.keanu365.studbud.TertiaryButton
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.launch
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_email
import studbud.composeapp.generated.resources.icon_lock
import studbud.composeapp.generated.resources.icon_user

@Composable
fun SignUpPage(
    modifier: Modifier = Modifier,
    onSignInClicked: (String, String) -> Unit,
    fromEmail: String = "",
    fromUsername: String = "",
    fromPassword: String = "",
    onSignIn: (User) -> Unit = {}
){
    val snackBarHostState = remember { SnackbarHostState() }
    val signUpScope = rememberCoroutineScope()

    var email by remember {mutableStateOf(fromEmail)}
    var username by remember { mutableStateOf(fromUsername) }
    var password by remember { mutableStateOf(fromPassword) }
    var confirmPassword by remember {mutableStateOf(password)}

    var isEmailError by remember {mutableStateOf(false)}
    var isUsernameError by remember {mutableStateOf(false)}
    var isPasswordError by remember {mutableStateOf(false)}
    var doesPasswordConflict by remember {mutableStateOf(false)}
    var submitAttempted by remember {mutableStateOf(false)}
    var buttonEnabled by remember {mutableStateOf(true)}
    fun performValidationChecks(): Boolean = run {
        isEmailError = !isEmailValid(email)
        isUsernameError = username.isBlank()
        isPasswordError = password.isBlank()
        doesPasswordConflict = password != confirmPassword
        !(isEmailError || isUsernameError || isPasswordError || doesPasswordConflict)
    }

    LaunchedEffect(email, username, password, confirmPassword){
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
            text = "Sign up to get started!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 15.dp)
                .focusable()
        )
        InfoField(
            value = email,
            onValueChange = {
                email = it
            },
            labelText = "Email",
            leadingIconResource = Res.drawable.icon_email,
            isError = isEmailError,
            errorText = "Invalid email"
        )
        InfoField(
            value = username,
            onValueChange = {
                username = it
            },
            labelText = "Username",
            leadingIconResource = Res.drawable.icon_user,
            isError = isUsernameError,
            errorText = "Please fill in a username!"
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
        InfoField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            labelText = "Confirm Password",
            leadingIconResource = Res.drawable.icon_lock,
            isError = doesPasswordConflict,
            isPassword = true,
            errorText = "Passwords do not match"
        )

        TertiaryButton(
            onClick = {
                submitAttempted = true
                buttonEnabled = performValidationChecks()
                if (performValidationChecks()) {
                    signUpScope.launch {
                        var snackBarMessage = ""
                        var signUpSuccessful = false
                        try {
                            buttonEnabled = false
                            signUp(email, username, password)
                            snackBarMessage = ""
                            signUpSuccessful = true
                        } catch(_: HttpRequestException){
                            snackBarMessage = "A network error occurred. Please check your connection and try again."
                        } catch (e: Exception) {
                            val errorMessage = e.message?.split(" ")[0]?.replace("_", " ")
                                ?: "Unknown error"
                            snackBarMessage = "Sign up failed: $errorMessage"
                            println(e.message)
                        } finally {
                            buttonEnabled = true
                            if (snackBarMessage.isNotBlank())
                                snackBarHostState.showSnackbar(snackBarMessage)
                            if (signUpSuccessful) {
                                val session = supabase.auth.currentSessionOrNull()
                                val userId = session?.user?.id ?: ""
                                val currentUser = User(
                                    id = userId,
                                    email = email,
                                    username = username
                                )
                                onSignIn(currentUser)
                            }
                        }
                    }
                }
            },
            enabled = buttonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .height(50.dp),
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
                        onSignInClicked(email, password)
                    }
            )
        }
        SnackbarHost(
            hostState = snackBarHostState
        )
    }
}