package io.github.keanu365.studbud

import androidx.compose.runtime.Composable

expect class SoundPlayer {
    fun playTimerFinishedSound()
}

@Composable
expect fun rememberSoundPlayer(): SoundPlayer