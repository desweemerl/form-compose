package com.desweemerl.compose.form

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test


class ValidationErrorTest {

    @Test
    fun checkPath() {
        assertEquals(Path("my", "path"), Path("my", "path"))
        assertEquals(Path("/my/path"), Path("my", "path"))
        assertArrayEquals(Path("/my/path").parts, arrayOf("my", "path"))

        val wrongPath = "//"
        try {
            Path(wrongPath)
            assert(false)
        } catch (e: Exception) {
            assert(e is ValidationException)
            assert(e.message == "path $wrongPath is incorrect")
        }
    }

    @Test
    fun checkPlus() {
        assertEquals(
            Path("/one/two/three/four"),
            Path("one", "two").plus(Path("three", "four"))
        )
    }
}