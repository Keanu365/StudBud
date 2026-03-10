package io.github.keanu365.studbud.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
    appPrefs: AppPreferences
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

    val coroutineScope = rememberCoroutineScope()
    fun onSignIn(user: User, key: NavKey) = run {
        coroutineScope.launch {
            appPrefs.saveSignIn(user.email, user.username)
            appPrefs.setFirstTimeUser(false)
            //TODO change this implementation when you do onboarding
        }
        backStack.add(Route.ThemeTest)
        backStack.remove(key)
    }
    fun onSignOut(key: NavKey) = run {
        coroutineScope.launch {
            supabase.auth.signOut()
            appPrefs.signOut()
        }
        backStack.add(Route.SignInPage)
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
                            }
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
                            onSignInClicked = {
                                backStack.remove(key)
                                backStack.add(Route.SignInPage)
                            },
                            onSignIn = { onSignIn(it, key) }
                        )
                    }
                }
                Route.SignInPage -> {
                    NavEntry(key) {
                        SignInPage(
                            onSignUpClicked = {
                                backStack.remove(key)
                                backStack.add(Route.SignUpPage)
                            },
                            onSignIn = { onSignIn(it, key) }
                        )
                    }
                }
                else -> error("Unknown route: $key")
            }
        },
        modifier = modifier
    )
}