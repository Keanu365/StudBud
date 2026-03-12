package io.github.keanu365.studbud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.keanu365.studbud.navigation.NavRoot
import io.github.keanu365.studbud.theme.StudBudTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_arrow_back
import studbud.composeapp.generated.resources.icon_leaderboard
import studbud.composeapp.generated.resources.icon_settings
import studbud.composeapp.generated.resources.studbud

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dataStore = remember { createDataStore() }
    val appPrefs = remember { AppPreferences(dataStore) }

    val snackBarHostState = remember { SnackbarHostState() }
    val mainAppScope = rememberCoroutineScope()

    var showTopBar by remember { mutableStateOf(false) }
    val topBarHeight = remember {
        if (getDeviceType() == "Phone") 100.dp
        else 70.dp
    }
    val spacerHeight = remember {
        if (getDeviceType() == "Phone") 80.dp
        else 70.dp
    }

    StudBudTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier.padding(bottom = 95.dp)
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    },
            ){
                Column(modifier = Modifier.padding(innerPadding)) {
                    Spacer(
                        modifier = Modifier.height(spacerHeight)
                    )
                    NavRoot(
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            },
                        appPrefs = appPrefs,
                        showSnackBar = { message ->
                            mainAppScope.launch {
                                snackBarHostState.showSnackbar(message)
                            }
                        },
                        showTopBar = {showTopBar = it}
                    )
                }
                //Top app bar
                AnimatedVisibility(
                    visible = showTopBar,
                    enter = slideInVertically(
                        animationSpec = tween(durationMillis = 500)
                    ){-it} + fadeIn(),
                    exit = slideOutVertically(
                        animationSpec = tween(durationMillis = 500)
                    ){-it} + fadeOut(),
                    modifier = Modifier.height(topBarHeight)
                ){
                    TopAppBar(
                        title = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ){
                                Icon(
                                    painter = painterResource(Res.drawable.studbud),
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .size(36.dp)
                                )
                                Text(
                                    text = "StudBud",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    //TODO
                                }
                            ){
                                Icon(
                                    painter = painterResource(Res.drawable.icon_arrow_back),
                                    contentDescription = "Back Arrow",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    //TODO
                                }
                            ){
                                Icon(
                                    painter = painterResource(Res.drawable.icon_leaderboard),
                                    contentDescription = "Leaderboard",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    //TODO
                                }
                            ){
                                Icon(
                                    painter = painterResource(Res.drawable.icon_settings),
                                    contentDescription = "Settings"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                    )
                }
            }
        }
    }
}