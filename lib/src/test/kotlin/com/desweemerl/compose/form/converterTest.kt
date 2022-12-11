package com.desweemerl.compose.form

import org.junit.Test
import kotlin.test.assertEquals

class ConverterTest {
    @Test
    fun `When a string that only contains only digits expect complete conversion of the string`() {
        assertEquals(stringToInt("123456789"), 123456789)
    }

    @Test
    fun `When a string that starts with minus and that only contains only digits expect complete conversion of the string`() {
        assertEquals(stringToInt("-123456789"), -123456789)
    }

    @Test
    fun `When a string that starts with minus and that only contains only digits expect null if negative value is not allowed`() {
        assertEquals(stringToInt("-123456789", allowNegative = false), null)
    }

    @Test
    fun `When a string that only contains mix of digits and letters expect conversion containing only digits`() {
        assertEquals(stringToInt("1a2b3ù456789"), 123456789)
    }

    @Test
    fun `When a string that equals to Int max expect conversion equals to Int max`() {
        assertEquals(stringToInt("2147483647"), 2147483647)
    }

    @Test
    fun `When a string that only contains digits that exceeds max int expect conversion containing limited digits`() {
        assertEquals(stringToInt("9999999999999"), 999999999)
    }
}