package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashScreenViewModel : ViewModel() {
    private val _tips = MutableStateFlow(
        listOf(
            "Smart suggestions can suggest assignments and study times for you!",
            "Join or start a group to share assignments with your friends!",
            "Compete against your friends on the group leaderboard!",
            "Studs are points given when you complete your study sessions!",
            "Each minute that you study earns you 1 stud!",
            "Need to hurry? Fret not - you can save your sessions for later!",
            "StudBud can help you remove distractions while studying!",
            "Thanks for using StudBud!",
            "Liking StudBud? Please leave a review!",
            "As StudBud is in its alpha stages, some things may break!",
            "StudBud uses the Poppins font for everything!",
            "You can use StudBud on your laptop too!",
            "StudBud was made with Kotlin Multiplatform!",
            "The StudBud logo is supposed to represent both S and δ!",
            "Brought to you by TheCoconutMan",
            "please speed i need this",
            "don't forget to like and subscribe",
            "pineapples",
            "i'm inside your walls",
            "hola"
        )
    )
    val tips = _tips.asStateFlow()
}