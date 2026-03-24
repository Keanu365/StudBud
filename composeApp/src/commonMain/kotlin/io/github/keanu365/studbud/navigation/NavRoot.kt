package io.github.keanu365.studbud.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.keanu365.studbud.AppPreferences
import io.github.keanu365.studbud.Assignment
import io.github.keanu365.studbud.Group
import io.github.keanu365.studbud.SplashLength
import io.github.keanu365.studbud.SplashScreen
import io.github.keanu365.studbud.SuccessPage
import io.github.keanu365.studbud.ThemeTest
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.account.SignInPage
import io.github.keanu365.studbud.account.SignUpPage
import io.github.keanu365.studbud.account.isEmailValid
import io.github.keanu365.studbud.main.AddAssignmentPage
import io.github.keanu365.studbud.main.AddGroupPage
import io.github.keanu365.studbud.main.AssignmentDetailsPage
import io.github.keanu365.studbud.main.GroupDetailsPage
import io.github.keanu365.studbud.main.Homepage
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.success

@Composable
fun NavRoot(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    appPrefs: AppPreferences,
    showSnackBar: (String) -> Unit,
    changeTopBar: (Boolean, Boolean, Boolean) -> Unit = {_, _, _ ->} //showBar, showBack, showActions
){
    val coroutineScope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var groupInFocus by remember { mutableStateOf<Group?>(null) }
    var assignmentInFocus by remember { mutableStateOf<Assignment?>(null) }

    var sharedEmail by remember {mutableStateOf("")}
    var sharedUsername by remember {mutableStateOf("")}
    var sharedPassword by remember {mutableStateOf("")}

    var splashLength by remember {mutableStateOf(SplashLength.MEDIUM)}
    fun onSignIn(user: User, key: NavKey) = run {
        coroutineScope.launch {
            appPrefs.saveSignIn(user.id)
            appPrefs.setFirstTimeUser(false)
            //TODO change this implementation when you do onboarding
        }
        sharedEmail = ""
        sharedUsername = ""
        sharedPassword = ""
        showSnackBar("Signed in successfully!")
        splashLength = SplashLength.SHORT
        backStack.add(Route.SplashScreen)
        backStack.remove(key)
    }
    fun onSignOut(key: NavKey) = run {
        coroutineScope.launch {
            supabase.auth.signOut()
            appPrefs.signOut()
        }
        splashLength = SplashLength.SHORT
        backStack.add(Route.SplashScreen)
        backStack.remove(key)
    }

    //SuccessPage stuff
    var successTitle by remember { mutableStateOf("") }
    var successImage by remember {mutableStateOf<@Composable () -> Unit>({})}
    var successContent by remember {mutableStateOf<@Composable () -> Unit>({})}

    //Indicate here if you want back arrow / actions
    val showBackKeys = listOf(
        Route.AddGroupPage,
        Route.GroupDetailsPage,
        Route.AssignmentDetailsPage
    )
    val showActionsKeys = listOf(
        Route.ThemeTest,
        Route.Homepage,
        Route.AddGroupPage,
        Route.GroupDetailsPage,
        Route.AssignmentDetailsPage
    )
    LaunchedEffect(backStack.last()){
        changeTopBar(
            backStack.last() != Route.SplashScreen,
            showBackKeys.contains(backStack.last()),
            showActionsKeys.contains(backStack.last())
        )
    }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            when(key){
                // Same here!
                Route.SplashScreen -> {
                    NavEntry(key){
                        SplashScreen(
                            modifier = Modifier.fillMaxSize(),
                            onEnd = {
                                coroutineScope.launch {
                                    try {
                                        val currentSession = supabase.auth.currentSessionOrNull()
                                        val nextRoute = if (currentSession != null || appPrefs.signedIn.first()) {
                                            Route.Homepage
                                        } else if (appPrefs.firstTimeUser.first()) {
                                            Route.SignUpPage
                                        } else {
                                            Route.SignInPage
                                        }
                                        backStack.add(nextRoute)
                                    } catch (e: HttpRequestException) {
                                        e.printStackTrace()
                                        if (appPrefs.signedIn.first()) backStack.add(Route.Homepage)
                                        else backStack.add(Route.SignInPage)
                                    } finally {
                                        backStack.remove(key)
                                    }
                                }
                            },
                            length = splashLength
                        )
                    }
                }
                Route.ThemeTest -> {
                    NavEntry(key) {
                        ThemeTest(
                            onReturn = {
                                backStack.remove(key)
                            },
                            onSignOut = { onSignOut(key) }
                        )
                    }
                }
                Route.SignUpPage -> {
                    NavEntry(key) {
                        SignUpPage(
                            onSignInClicked = { emailOrUsername, password ->
                                backStack.remove(key)
                                backStack.add(Route.SignInPage)
                                if (isEmailValid(emailOrUsername)) sharedEmail = emailOrUsername
                                else sharedUsername = emailOrUsername
                                sharedPassword = password
                            },
                            onSignIn = { onSignIn(it, key) },
                            fromEmail = sharedEmail,
                            fromUsername = sharedUsername,
                            fromPassword = sharedPassword
                        )
                    }
                }
                Route.SignInPage -> {
                    NavEntry(key) {
                        SignInPage(
                            onSignUpClicked = { email, password ->
                                backStack.remove(key)
                                backStack.add(Route.SignUpPage)
                                sharedEmail = email
                                sharedPassword = password
                            },
                            onSignIn = { onSignIn(it, key) },
                            fromEmail = sharedEmail,
                            fromPassword = sharedPassword
                        )
                    }
                }
                Route.Homepage -> {
                    NavEntry(key){
                        Homepage(
                            onSignOut = { onSignOut(key) },
                            appPrefs = appPrefs,
                            showSnackBar = {showSnackBar(it)},
                            onUserLoaded = {user = it},
                            onAddGroup = {
                                user = it
                                backStack.add(Route.AddGroupPage)
                            },
                            onGroupClicked = {
                                groupInFocus = it
                                backStack.add(Route.GroupDetailsPage)
                            },
                            onAssignmentClicked = {
                                assignmentInFocus = it
                                backStack.add(Route.AssignmentDetailsPage)
                            },
                            onAssignmentAdd = {
                                user = it
                                backStack.add(Route.AddAssignmentPage)
                            },
                            onTimerStart = {
                                //TODO
                                showSnackBar(it?.id ?: "No assignment selected")
                            }
                        )
                    }
                }
                Route.AddGroupPage -> {
                    NavEntry(key) {
                        AddGroupPage(
                            user = user ?: error("User is null"),
                            onJoin = {
                                successTitle = "Group joined/created successfully!"
                                successImage = {
                                    Image(
                                        painter = painterResource(Res.drawable.success),
                                        contentDescription = null,
                                    )
                                }
                                successContent = {GroupDetailsPage(it, modifier = Modifier)}
                                backStack.remove(key)
                                backStack.add(Route.SuccessPage)
                            },
                            showSnackBar = {showSnackBar(it)}
                        )
                    }
                }
                Route.GroupDetailsPage -> {
                    NavEntry(key) {
                        GroupDetailsPage(
                            group = groupInFocus ?: error("Group is null"),
                            onAssignmentClicked = {
                                assignmentInFocus = it
                                //Clean up and remove previous assignment details
                                backStack.remove(Route.AssignmentDetailsPage)
                                backStack.add(Route.AssignmentDetailsPage)
                            },
                            onAssignmentAdd = {
                                backStack.add(Route.AddAssignmentPage)
                            }
                        )
                    }
                }
                Route.AssignmentDetailsPage -> {
                    NavEntry(key) {
                        AssignmentDetailsPage(
                            assignment = assignmentInFocus ?: error("Assignment is null"),
                            onGroupClicked = {
                                groupInFocus = it
                                //Clean up and remove previous group details
                                backStack.remove(Route.GroupDetailsPage)
                                backStack.add(Route.GroupDetailsPage)
                            }
                        )
                    }
                }
                Route.AddAssignmentPage -> {
                    NavEntry(key) {
                        AddAssignmentPage(
                            startingGroup = groupInFocus,
                            user = user ?: error("User is null"),
                            showSnackBar = {showSnackBar(it)},
                            onAdd = {
                                successTitle = "Assignment created!"
                                successImage = {
                                    Image(
                                        painter = painterResource(Res.drawable.success),
                                        contentDescription = null,
                                    )
                                }
                                successContent = {
                                    AssignmentDetailsPage(it, modifier = Modifier)
                                }
                                backStack.remove(key)
                                backStack.add(Route.SuccessPage)
                            }
                        )
                    }
                }
                Route.SuccessPage -> {
                    NavEntry(key) {
                        SuccessPage(
                            title = successTitle,
                            image = successImage,
                            content = successContent,
                            onReturn = {
                                backStack.remove(key)
                                backStack.add(Route.Homepage)
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(15.dp)
                                .padding(bottom = 20.dp)
                        )
                    }
                }
                else -> error("Unknown route: $key")
            }
        },
        modifier = modifier
    )
}