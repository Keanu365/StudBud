package io.github.keanu365.studbud.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import org.jetbrains.compose.resources.DrawableResource
import studbud.composeapp.generated.resources.Res
import studbud.composeapp.generated.resources.onb2
import studbud.composeapp.generated.resources.onb3
import studbud.composeapp.generated.resources.onb4
import studbud.composeapp.generated.resources.onb5
import studbud.composeapp.generated.resources.onb6
import studbud.composeapp.generated.resources.studbud

class OnboardingViewModel: ViewModel() {
    val pages = listOf(
        OnboardingPage(
            bgColor = Color(0xFF072A20),
            title = "Welcome to StudBud!",
            desc = "Get ready to stud(y) and bud!",
            resource = Res.drawable.studbud
        ),
        OnboardingPage(
            bgColor = Color(0xFF041123),
            title = "Study with Friends!",
            desc = "Join or create groups for maximum collaborative learning!",
            resource = Res.drawable.onb2
        ),
        OnboardingPage(
            bgColor = Color(0xFF013D05),
            title = "Add Assignments!",
            desc = "Make some assignments and share them with your friends!",
            resource = Res.drawable.onb3
        ),
        OnboardingPage(
            bgColor = Color(0xFF453A0C),
            title = "STUDY!",
            desc = "And of course, what the app is truly for! Modelled after the renowned Pomodoro Method!",
            resource = Res.drawable.onb4
        ),
        OnboardingPage(
            bgColor = Color(0xFF200040),
            title = "Achieve!",
            desc = "Achievements and studs, specially designed for maximum dopamine.",
            resource = Res.drawable.onb5
        ),
        OnboardingPage(
            bgColor = Color(0xFF210000),
            title = "Compete!",
            desc = "Foster the competitive spirit in you!",
            resource = Res.drawable.onb6
        ),
    )
}

data class OnboardingPage(
    val bgColor: Color,
    val title: String,
    val desc: String,
    val resource: DrawableResource
)