package io.github.keanu365.studbud

import android.annotation.SuppressLint
import android.content.Context
import androidx.startup.Initializer

// A simple singleton to hold our context
@SuppressLint("StaticFieldLeak") // Safe because we only store the ApplicationContext
object AppContext {
    private var value: Context? = null

    fun set(context: Context) {
        // Always store the applicationContext to avoid leaking Activities
        value = context.applicationContext
    }

    fun get(): Context = value ?: error("Context not initialized!")
}

// Automatically initializes the context so you don't have to manually pass it
class AppContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context {
        AppContext.set(context)
        return context
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}