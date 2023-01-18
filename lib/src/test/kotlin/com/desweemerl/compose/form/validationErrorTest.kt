package com.desweemerl.compose.form

import com.desweemerl.compose.form.controls.FormFieldState
import com.desweemerl.compose.form.validators.ValidatorRequired
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals


class ValidationErrorTest {
    @Test
    @ExperimentalCoroutinesApi
    fun `When 2 validation errors have same path and same type expect to be equals`() = runTest {
        val dummyState = FormFieldState(value = "")
        val expectation = ValidatorRequired().validate(dummyState)
        val actual = ValidatorRequired("message update").validate(dummyState)
        assertEquals(expectation, actual)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When validation errors are merged expect list including path defined in merge`() {
        val expectation = listOf(
            ValidationError(type = "dummyError1", message = "dummy message1", path = Path()),
            ValidationError(type = "dummyError2", message = "dummy message2", path = Path("path1")),
            ValidationError(type = "dummyError3", message = "dummy message3", path = Path("path1")),
            ValidationError(type = "dummyError4", message = "dummy message4", path = Path("path2")),
        )
        val actual =
            listOf(ValidationError(type = "dummyError1", message = "dummy message1", path = Path()))
                .replace(
                    "path1",
                    listOf(
                        ValidationError(type = "dummyError2", message = "dummy message2"),
                        ValidationError(type = "dummyError3", message = "dummy message3"),
                    )
                )
                .replace(
                    "path2",
                    listOf(ValidationError(type = "dummyError4", message = "dummy message4"))
                )

        assertMatchErrors(expectation, actual)
    }
}