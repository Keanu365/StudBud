package io.github.keanu365.studbud

actual fun producePath(): String = AppContext.get().filesDir.resolve(dataStoreFileName).absolutePath