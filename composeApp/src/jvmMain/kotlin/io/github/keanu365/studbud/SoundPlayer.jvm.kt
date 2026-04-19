package io.github.keanu365.studbud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.sound.sampled.AudioSystem
import java.io.BufferedInputStream

actual class SoundPlayer {
    actual fun playTimerFinishedSound() {
        try {
            val inputStream = javaClass.getResourceAsStream("/alarm.wav")
            val bufferedIn = BufferedInputStream(inputStream)
            val audioIn = AudioSystem.getAudioInputStream(bufferedIn)
            val clip = AudioSystem.getClip()
            clip.open(audioIn)
            clip.start()
            // Auto-close line when done
            clip.addLineListener { event ->
                if (event.type == javax.sound.sampled.LineEvent.Type.STOP) {
                    clip.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer = remember { SoundPlayer() }