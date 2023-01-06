package com.desweemerl.compose.form

import org.junit.Assert
import org.junit.Test

class PathTest {

    @Test
    fun `When 2 paths contain same value expect that they are equals`() {
        Assert.assertEquals(Path("my", "path"), Path("my", "path"))
    }

    @Test
    fun `When a path is initialized with a string containing paths separated with slashes expect the string is correctly parsed`() {
        Assert.assertEquals(Path("/my/path"), Path("my", "path"))
        Assert.assertArrayEquals(Path("/my/path").parts, arrayOf("my", "path"))
    }

    @Test
    fun `When a path is initialized with a string containing 2 consecutive slashes expect a validation exception to be thrown`() {
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
    fun `When 2 paths are added expect the result to be a combination of these paths`() {
        Assert.assertEquals(
            Path("/one/two/three/four"),
            Path("one", "two").plus(Path("three", "four"))
        )
    }
}