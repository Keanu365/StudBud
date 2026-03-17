package io.github.keanu365.studbud.navigation

import androidx.compose.foundation.layout.fillMaxSize
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
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.keanu365.studbud.AppPreferences
import io.github.keanu365.studbud.SplashLength
import io.github.keanu365.studbud.SplashScreen
import io.github.keanu365.studbud.ThemeTest
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.account.SignInPage
import io.github.keanu365.studbud.account.SignUpPage
import io.github.keanu365.studbud.account.isEmailValid
import io.github.keanu365.studbud.main.Homepage
import io.github.keanu365.studbud.supabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun NavRoot(
    modifier: Modifier = Modifier,
    appPrefs: AppPreferences,
    showSnackBar: (String) -> Unit,
    showTopBar: (Boolean) -> Unit = {}
){
    val coroutineScope = rememberCoroutineScope()
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
                    subclass(Route.Homepage::class, Route.Homepage.serializer())
                }
            }
        },
        Route.SplashScreen
    )

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
                        showTopBar(false)
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
                                        showTopBar(true)
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
                            appPrefs = appPrefs
                        )
                    }
                }
                else -> error("Unknown route: $key")
            }
        },
        modifier = modifier
    )
}