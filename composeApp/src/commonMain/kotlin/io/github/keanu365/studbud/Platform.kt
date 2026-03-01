package io.github.keanu365.studbud

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform