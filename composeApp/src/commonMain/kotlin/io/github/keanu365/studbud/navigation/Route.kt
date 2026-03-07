package io.github.keanu365.studbud.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey {

    @Serializable
    data object SplashScreen: Route, NavKey
    @Serializable
    data object ThemeTest: Route, NavKey //REMOVE ONCE START ON ACTUAL PROJECT


}