package io.github.keanu365.studbud.account

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.keanu365.studbud.User
import io.github.keanu365.studbud.supabase
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val emailAddressRegex = Regex(
    "[a-zA-Z0-9+._%\\-@]{1,256}" +
    "@" +
    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
    "(" +
    "\\." +
    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
    ")"
)

fun isEmailValid(email: String): Boolean {
    return email.matches(emailAddressRegex)
}

suspend fun signUp(
    email: String,
    username: String,
    password: String,
){
    supabase.auth.signUpWith(Email){
        this.email = email
        this.password = password
        this.data = buildJsonObject {
            put("username", username)
        }
    }
}

suspend fun signIn(
    emailOrUsername: String,
    password: String
): User {
    // 1. If it's an email, we can sign in to Auth immediately to establish the session
    val initialEmail = if (isEmailValid(emailOrUsername)) emailOrUsername else null
    if (initialEmail != null) {
        supabase.auth.signInWith(Email) {
            this.email = initialEmail
            this.password = password
        }
    }

    // 2. Fetch the profile with a retry mechanism
    var user: User? = null
    var attempts = 0
    while (user == null && attempts < 5) {
        try {
            user = supabase.from("profiles")
                .select {
                    filter {
                        if (isEmailValid(emailOrUsername)) {
                            eq("email", emailOrUsername)
                        } else {
                            eq("username", emailOrUsername)
                        }
                    }
                }
                .decodeSingleOrNull<User>()
        } catch (_: Exception) {
            // Log error or ignore for retry
        }

        if (user == null) {
            attempts++
            if (attempts < 3) kotlinx.coroutines.delay(200) // Wait 200ms before retry
        }
    }

    val finalUser = user ?: throw Exception("Profile not found. Please check your credentials.")

    // 3. If we used a username, we sign in now using the email we just found
    if (initialEmail == null) {
        supabase.auth.signInWith(Email) {
            this.email = finalUser.email
            this.password = password
        }
    }

    return finalUser
}