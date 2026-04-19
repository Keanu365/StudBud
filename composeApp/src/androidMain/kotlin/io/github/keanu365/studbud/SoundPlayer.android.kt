package io.github.keanu365.studbud

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.keanu365.studbud.common.R

actual class SoundPlayer(private val context: android.content.Context) {
    actual fun playTimerFinishedSound() {
        try {
            println("Playing sound!")
            val mediaPlayer = MediaPlayer.create(context, R.raw.alarm)

            mediaPlayer.setOnCompletionListener { mp ->
                mp.reset()
                mp.release()
            }

            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    val context = LocalContext.current
    return remember(context) { SoundPlayer(context) }
}