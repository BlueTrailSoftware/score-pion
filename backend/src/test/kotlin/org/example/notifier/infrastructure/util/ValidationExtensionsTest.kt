package org.example.notifier.infrastructure.util

import org.example.notifier.application.util.isValidEmail
import org.example.notifier.application.util.isValidLinkedInUrl
import org.example.notifier.application.util.isValidPhone
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ValidationExtensionsTest {

    // --- isValidEmail ---

    @Test
    fun `isValidEmail accepts standard email`() {
        assertTrue("user@example.com".isValidEmail())
    }

    @Test
    fun `isValidEmail accepts email with plus addressing`() {
        assertTrue("user+tag@example.com".isValidEmail())
    }

    @Test
    fun `isValidEmail accepts email with subdomain`() {
        assertTrue("user@mail.example.co.uk".isValidEmail())
    }

    @Test
    fun `isValidEmail rejects missing at sign`() {
        assertFalse("userexample.com".isValidEmail())
    }

    @Test
    fun `isValidEmail rejects missing TLD`() {
        assertFalse("user@example".isValidEmail())
    }

    @Test
    fun `isValidEmail rejects spaces`() {
        assertFalse("user @example.com".isValidEmail())
    }

    @Test
    fun `isValidEmail rejects empty string`() {
        assertFalse("".isValidEmail())
    }

    // --- isValidPhone ---

    @Test
    fun `isValidPhone accepts international format`() {
        assertTrue("+54 11 1234-5678".isValidPhone())
    }

    @Test
    fun `isValidPhone accepts parentheses and hyphens`() {
        assertTrue("(011) 1234-5678".isValidPhone())
    }

    @Test
    fun `isValidPhone accepts minimum length digits`() {
        assertTrue("1234567".isValidPhone())
    }

    @Test
    fun `isValidPhone rejects too short`() {
        assertFalse("123456".isValidPhone())
    }

    @Test
    fun `isValidPhone rejects letters`() {
        assertFalse("123-CALL-ME".isValidPhone())
    }

    @Test
    fun `isValidPhone rejects empty string`() {
        assertFalse("".isValidPhone())
    }

    @Test
    fun `isValidPhone accepts maximum length`() {
        assertTrue("+12345678901234567890".isValidPhone()) // + plus 20 digits
    }

    @Test
    fun `isValidPhone rejects over maximum length`() {
        assertFalse("+123456789012345678901".isValidPhone()) // + plus 21 digits
    }

    // --- isValidLinkedInUrl ---

    @Test
    fun `isValidLinkedInUrl accepts https with www`() {
        assertTrue("https://www.linkedin.com/in/johndoe".isValidLinkedInUrl())
    }

    @Test
    fun `isValidLinkedInUrl accepts https without www`() {
        assertTrue("https://linkedin.com/in/johndoe".isValidLinkedInUrl())
    }

    @Test
    fun `isValidLinkedInUrl accepts http`() {
        assertTrue("http://linkedin.com/in/johndoe".isValidLinkedInUrl())
    }

    @Test
    fun `isValidLinkedInUrl rejects non-linkedin domain`() {
        assertFalse("https://github.com/johndoe".isValidLinkedInUrl())
    }

    @Test
    fun `isValidLinkedInUrl rejects plain text without protocol`() {
        assertFalse("linkedin.com/in/johndoe".isValidLinkedInUrl())
    }

    @Test
    fun `isValidLinkedInUrl rejects empty string`() {
        assertFalse("".isValidLinkedInUrl())
    }
}
