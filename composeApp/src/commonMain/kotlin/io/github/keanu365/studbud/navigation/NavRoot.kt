package io.github.keanu365.studbud.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.jan.supabase.auth.auth
import io.github.keanu365.studbud.*
import io.github.keanu365.studbud.account.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun NavRoot(
    modifier: Modifier = Modifier,
    appPrefs: AppPreferences,
    showSnackBar: (String) -> Unit
){
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration{
            serializersModule = SerializersModule{
                polymorphic(NavKey::class){
                    // All future screens need to be added here,
                    // like intents in AndroidManifest.xml.
                    // Don't forget to do so at Line 35 too!
                    // Oh, and go serialize this in Route.kt before doing this
                    subclass(Route.SplashScreen::class, Route.SplashScreen.serializer())
                    subclass(Route.ThemeTest::class, Route.ThemeTest.serializer())
                    subclass(Route.SignUpPage::class, Route.SignUpPage.serializer())
                    subclass(Route.SignInPage::class, Route.SignInPage.serializer())
                    subclass(Route.SupabaseTest::class, Route.SupabaseTest.serializer())
                }
            }
        },
        Route.SplashScreen
    )

    var sharedEmail by remember {mutableStateOf("")}
    var sharedUsername by remember {mutableStateOf("")}
    var sharedPassword by remember {mutableStateOf("")}

    val coroutineScope = rememberCoroutineScope()
    var splashLength by remember {mutableStateOf(SplashLength.MEDIUM)}
    fun onSignIn(user: User, key: NavKey) = run {
        coroutineScope.launch {
            appPrefs.saveSignIn(user.email, user.username)
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
        showSnackBar("Signed out successfully!")
        splashLength = SplashLength.SHORT
        backStack.add(Route.SplashScreen)
        backStack.remove(key)
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
                            onEnd = {
                                coroutineScope.launch {
                                    if (appPrefs.signedIn.first()) {
                                        backStack.add(Route.ThemeTest)
                                    } else if (appPrefs.firstTimeUser.first()) {
                                        backStack.add(Route.SignUpPage)
                                    } else {
                                        backStack.add(Route.SignInPage)
                                    }
                                    backStack.remove(key)
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
                Route.SupabaseTest -> {
                    NavEntry(key) {
                        SampleData()
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
                else -> error("Unknown route: $key")
            }
        },
        modifier = modifier
    )
}