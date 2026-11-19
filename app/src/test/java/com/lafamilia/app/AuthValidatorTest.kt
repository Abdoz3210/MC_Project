package com.lafamilia.app

import com.lafamilia.app.domain.AuthError
import com.lafamilia.app.domain.AuthValidator
import com.lafamilia.app.domain.PasswordStrength
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validEmailsAreAccepted() {
        assertTrue(AuthValidator.isValidEmail("a.b@mail.com"))
        assertTrue(AuthValidator.isValidEmail("  user+tag@sub.domain.org "))
    }

    @Test
    fun invalidEmailsAreRejected() {
        assertFalse(AuthValidator.isValidEmail("abc"))
        assertFalse(AuthValidator.isValidEmail("a@b"))
        assertFalse(AuthValidator.isValidEmail("@mail.com"))
    }

    @Test
    fun loginNeedsBothFields() {
        assertEquals(AuthError.EMPTY_FIELDS, AuthValidator.validateLogin("", "x"))
        assertEquals(AuthError.EMPTY_FIELDS, AuthValidator.validateLogin("a@b.com", ""))
        assertEquals(AuthError.INVALID_EMAIL, AuthValidator.validateLogin("nope", "x"))
        assertNull(AuthValidator.validateLogin("a@b.com", "x"))
    }

    @Test
    fun registerChecksInOrder() {
        assertEquals(AuthError.NAME_REQUIRED, AuthValidator.validateRegister("", "a@b.com", "123456", "123456"))
        assertEquals(AuthError.INVALID_EMAIL, AuthValidator.validateRegister("Ann", "bad", "123456", "123456"))
        assertEquals(AuthError.PASSWORD_TOO_SHORT, AuthValidator.validateRegister("Ann", "a@b.com", "123", "123"))
        assertEquals(AuthError.PASSWORDS_DO_NOT_MATCH, AuthValidator.validateRegister("Ann", "a@b.com", "123456", "654321"))
        assertNull(AuthValidator.validateRegister("Ann", "a@b.com", "123456", "123456"))
    }

    @Test
    fun passwordStrengthLevels() {
        assertEquals(PasswordStrength.WEAK, AuthValidator.passwordStrength("abc"))
        assertEquals(PasswordStrength.WEAK, AuthValidator.passwordStrength("abcdef"))
        assertEquals(PasswordStrength.MEDIUM, AuthValidator.passwordStrength("abcde1"))
        assertEquals(PasswordStrength.STRONG, AuthValidator.passwordStrength("Abcdefg123!"))
    }
}
