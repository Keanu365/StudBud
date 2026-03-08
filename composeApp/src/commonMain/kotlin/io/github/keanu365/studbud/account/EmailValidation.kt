package io.github.keanu365.studbud.account

// In a common/shared module file (e.g., EmailValidator.kt)
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