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
    data object SignUpPage: Route, NavKey
    @Serializable
    data object SignInPage: Route, NavKey
    @Serializable
    data object Homepage: Route, NavKey
    @Serializable
    data object AddGroupPage: Route, NavKey
    @Serializable
    data object GroupDetailsPage: Route, NavKey
    @Serializable
    data object EditGroupPage: Route, NavKey
    @Serializable
    data object AssignmentDetailsPage: Route, NavKey
    @Serializable
    data object EditAssignmentPage: Route, NavKey
    @Serializable
    data object SuccessPage: Route, NavKey
    @Serializable
    data object AddAssignmentPage: Route, NavKey
    @Serializable
    data object TimerPage: Route, NavKey
    @Serializable
    data object TimerDetailsPage: Route, NavKey
    @Serializable
    data object Leaderboard: Route, NavKey
    @Serializable
    data object SettingsPage: Route, NavKey
    @Serializable
    data object ImageViewPage: Route, NavKey
    @Serializable
    data object AchievementsPage: Route, NavKey
}