package io.github.keanu365.studbud.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import io.github.keanu365.studbud.*
import io.github.keanu365.studbud.account.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Composable
fun NavRoot(
    modifier: Modifier = Modifier
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
                                backStack.remove(key)
                                backStack.add(Route.SignUpPage)
//                                For Supabase testing. Currently works.
//                                backStack.add(Route.SupabaseTest)
//                                TODO: Logic for sign up / sign in / onboarding / homepage
                            }
                        )
                    }
                }
                Route.ThemeTest -> {
                    NavEntry(key) {
                        ThemeTest(
                            onReturn = {
                                backStack.remove(key)
                            }
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
                            }
                        )
                    }
                }
                Route.SignInPage -> {
                    NavEntry(key) {
                        SignInPage(
                            onSignUpClicked = {
                                backStack.remove(key)
                                backStack.add(Route.SignUpPage)
                            }
                        )
                    }
                }
                else -> error("Unknown route: $key")
            }
        },
        modifier = modifier
    )
}
