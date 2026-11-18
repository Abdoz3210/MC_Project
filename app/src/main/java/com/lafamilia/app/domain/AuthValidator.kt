package com.lafamilia.app.domain

enum class AuthError { EMPTY_FIELDS, NAME_REQUIRED, INVALID_EMAIL, PASSWORD_TOO_SHORT, PASSWORDS_DO_NOT_MATCH }

enum class PasswordStrength { WEAK, MEDIUM, STRONG }

/**
 * Pure Kotlin validation rules (no Android classes), so they are easy to unit test.
 * Every function returns the FIRST problem found, or null when the input is fine.
 */
object AuthValidator {

    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

    fun validateLogin(email: String, password: String): AuthError? = when {
        email.isBlank() || password.isBlank() -> AuthError.EMPTY_FIELDS
        !isValidEmail(email) -> AuthError.INVALID_EMAIL
        else -> null
    }

    fun validateRegister(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): AuthError? = when {
        name.isBlank() -> AuthError.NAME_REQUIRED
        !isValidEmail(email) -> AuthError.INVALID_EMAIL
        password.length < MIN_PASSWORD_LENGTH -> AuthError.PASSWORD_TOO_SHORT
        password != confirmPassword -> AuthError.PASSWORDS_DO_NOT_MATCH
        else -> null
    }

    fun passwordStrength(password: String): PasswordStrength {
        if (password.length < MIN_PASSWORD_LENGTH) return PasswordStrength.WEAK
        val score = listOf(
            password.length >= 10,
            password.any { it.isDigit() },
            password.any { it.isUpperCase() } && password.any { it.isLowerCase() },
            password.any { !it.isLetterOrDigit() }
        ).count { it }
        return when {
            score >= 3 -> PasswordStrength.STRONG
            score >= 1 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
}
