package io.github.keanu365.studbud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

actual class SoundPlayer {
    private var player: AVAudioPlayer? = null

    @OptIn(ExperimentalForeignApi::class)
    actual fun playTimerFinishedSound() {
        val bundle = NSBundle.mainBundle
        val path = bundle.pathForResource("alarm", "wav")

        if (path != null) {
            val url = NSURL.fileURLWithPath(path)
            try {
                // In Kotlin/Native, the constructor often looks like this:
                player = AVAudioPlayer(contentsOfURL = url, error = null)
                player?.prepareToPlay()
                player?.play()
            } catch (e: Exception) {
                println("iOS Audio Error: ${e.message}")
            }
        } else {
            println("iOS Audio Error: alarm.wav not found in bundle")
        }
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer = remember { SoundPlayer() }