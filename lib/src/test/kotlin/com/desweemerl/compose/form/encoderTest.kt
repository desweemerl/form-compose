package com.desweemerl.compose.form

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JsonEncoderTest {

    @Test
    fun `When a map containing string as key and allowed type as value expect JsonElement generated`() {
        val value = mapOf(
            "key1" to "value1",
            "key2" to null,
            "key3" to 1,
            "key4" to true,
            "key5" to mapOf(
                "subkey1" to "subvalue1"
            )
        )
        val actual = valueJsonEncoder(value).toString()
        val expected = "{\"key1\":\"value1\",\"key2\":null,\"key3\":1,\"key4\":true,\"key5\":{\"subkey1\":\"subvalue1\"}}"
        assertEquals(actual, expected)
    }

    @Test
    fun `When a map containing a key with a type other than string expect exception`() {
        assertFailsWith<Exception> {
            val value = mapOf(
                1 to "value1",
            )
            valueJsonEncoder(value)
        }
    }

    @Test
    fun `When a map containing a value with a unsupported type expect exception`() {
        assertFailsWith<Exception> {
            val value = mapOf(
                "key1" to arrayOf(1, 2, 3)
            )
            valueJsonEncoder(value)
        }
    }
}