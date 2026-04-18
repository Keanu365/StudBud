package io.github.keanu365.studbud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.keanu365.studbud.navigation.NavRoot
import io.github.keanu365.studbud.navigation.Route
import io.github.keanu365.studbud.theme.StudBudTheme
import io.github.keanu365.studbud.viewmodels.MainViewModel
import io.github.keanu365.studbud.viewmodels.SettingsViewModel
import io.github.keanu365.studbud.viewmodels.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import my.connectivity.kmp.data.model.NetworkStatus
import my.connectivity.kmp.rememberNetworkStatus
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.icon_ach
import studbud.composeapp.generated.resources.icon_arrow_back
import studbud.composeapp.generated.resources.icon_leaderboard
import studbud.composeapp.generated.resources.icon_settings
import studbud.composeapp.generated.resources.studbud
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    appPrefs: AppPreferences,
    viewModel: MainViewModel = viewModel { MainViewModel(appPrefs) }
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val snackBarHostState = remember { SnackbarHostState() }
    val mainAppScope = rememberCoroutineScope()

    val backStack = viewModel.rememberBackStack()
    val currentRoute = backStack.last()
    val showTopBar = !viewModel.hideTopBar.contains(currentRoute)
    val showBackArrow = viewModel.showBackKeys.contains(currentRoute)
    val showActions = viewModel.showActionsKeys.contains(currentRoute)

    val topBarHeight = if (getDeviceType() == "Phone") 100.dp else 70.dp
    val spacerHeight = if (getDeviceType() == "Phone") 60.dp else 70.dp

    val isNetworkAvailable by rememberNetworkStatus()
    var networkText by remember {mutableStateOf("No Internet Connection")}
    var networkVisible by remember {mutableStateOf(false)}
    var hasShownFirstConnection by remember { mutableStateOf(false) }
    LaunchedEffect(isNetworkAvailable) {
        if (isNetworkAvailable == NetworkStatus.Available) {
            if (!hasShownFirstConnection) {
                hasShownFirstConnection = true
                networkVisible = false
            } else {
                if (networkVisible) {
                    networkText = "Connected to Internet!"
                    delay(5000)
                    networkVisible = false
                }
            }
        } else {
            networkText = "No Internet Connection"
            networkVisible = true
            hasShownFirstConnection = true
        }
    }

    val settingsViewModel = viewModel { SettingsViewModel(appPrefs = appPrefs) }
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

    StudBudTheme(
        darkTheme = when(settings.theme){
            Theme.LIGHT -> false
            Theme.DARK -> true
            Theme.SYSTEM -> isSystemInDarkTheme()
            Theme.TIME -> Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time.hour !in 7..19
        }
    ) {
        val networkBgAnimation = animateColorAsState(
            targetValue = if (isNetworkAvailable == NetworkStatus.Available) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface,
            animationSpec = tween(durationMillis = 500)
        )
        val networkTextColorAnimation = animateColorAsState(
            targetValue = if (isNetworkAvailable == NetworkStatus.Available) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            animationSpec = tween(durationMillis = 500)
        )
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
                    if (backStack.last() !in viewModel.hideTopBar) Spacer(
                        modifier = Modifier.height(spacerHeight)
                    )
                    AnimatedVisibility(
                        visible = networkVisible && showTopBar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(networkBgAnimation.value)
                    ){
                        Column{
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = networkText,
                                color = networkTextColorAnimation.value,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                            Spacer(Modifier.height(5.dp))
                        }
                    }
                    if (getDeviceType() != "Phone") Spacer(Modifier.height(15.dp))
                    NavRoot(
                        viewModel = viewModel,
                        backStack = backStack,
                        settingsViewModel = settingsViewModel,
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            },
                        showSnackBar = { message ->
                            mainAppScope.launch {
                                snackBarHostState.showSnackbar(message)
                            }
                        }
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
                            if (showBackArrow)
                            IconButton(
                                onClick = {
                                    backStack.removeAt(backStack.size - 1)
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
                            if (showActions){
                                IconButton(
                                    onClick = {
                                        backStack.add(Route.AchievementsPage)
                                    }
                                ){
                                    Icon(
                                        painter = painterResource(Res.drawable.icon_ach),
                                        contentDescription = "Settings"
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (isNetworkAvailable == NetworkStatus.Available) backStack.add(Route.Leaderboard)
                                        else mainAppScope.launch{
                                            snackBarHostState.showSnackbar("Please connect to the internet!")
                                        }
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
                                        backStack.add(Route.SettingsPage)
                                    }
                                ){
                                    Icon(
                                        painter = painterResource(Res.drawable.icon_settings),
                                        contentDescription = "Settings"
                                    )
                                }
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