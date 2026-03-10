package io.github.keanu365.studbud

import java.io.File

actual fun producePath(): String {
    val file = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
    return file.absolutePath
}