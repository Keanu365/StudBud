package io.github.keanu365.studbud.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey {
    @Serializable
    data object SplashScreen: Route, NavKey
    @Serializable
    data object ThemeTest: Route, NavKey
    @Serializable
    data object SupabaseTest: Route, NavKey
    @Serializable
    data object SignUpPage: Route, NavKey
    @Serializable
    data object SignInPage: Route, NavKey
    @Serializable
    data object Homepage: Route, NavKey
}