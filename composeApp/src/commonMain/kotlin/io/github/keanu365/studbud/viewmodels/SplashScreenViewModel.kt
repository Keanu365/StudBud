package io.github.keanu365.studbud.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashScreenViewModel : ViewModel() {
    //Continue watching https://youtu.be/G_e9XL5tI6U?si=EWVbB3cCQddgH522
    private val _tips = MutableStateFlow(
        listOf(
            "Tips 1",
            "Tips 2",
            "Tips 3"
        )
    )
    val tips = _tips.asStateFlow()
}