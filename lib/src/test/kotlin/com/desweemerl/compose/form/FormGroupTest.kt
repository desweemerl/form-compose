package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.ValidatorPattern
import com.desweemerl.compose.form.validators.ValidatorRequired
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test


class FormGroupTest {
    @Test
    @ExperimentalCoroutinesApi
    fun formGroupValues() = runTest {
        val form = FormGroupBuilder()
            .withControl("first_name", textControl(""))
            .withControl("last_name", textControl())
            .build()

        var actualValue = mapOf<String, Any>()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            form.state.collect { state ->
                actualValue = state.value
            }
        }

        val expectation = mutableMapOf(
            Pair("first_name", ""),
            Pair("last_name", ""),
        )

        assertEquals(expectation, actualValue)

        form.getControl("first_name")?.transformValue { "test" }
        expectation["first_name"] = "test"

        form.transformValue { value ->
            value.plus(Pair("new_field", "new_value"))
        }

        expectation["new_field"] = "new_value"
        assertEquals(expectation, actualValue)

        form.transformValue { value ->
            value.plus(Pair("first_name", "${value["first_name"]}_bis"))
        }

        expectation["first_name"] = "test_bis"
        assertEquals(expectation, actualValue)

        collectJob.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun formGroupNested() = runTest {
        val detailsForm = FormGroupBuilder()
            .withControl("option", textControl(""))
            .build()


        val form = FormGroupBuilder()
            .withControl("first_name", textControl(""))
            .withControl("last_name", textControl())
            .withControl("details", detailsForm)
            .build()

        var actualValue = mapOf<String, Any>()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            form.state.collect { state ->
                actualValue = state.value
            }
        }

        val expectationDetails = mutableMapOf<String, Any>(
            Pair("option", "")
        )

        val expectation = mutableMapOf(
            Pair("first_name", ""),
            Pair("last_name", ""),
            Pair("details", expectationDetails),
        )

        assertEquals(expectation, actualValue)

        detailsForm.transformValue { value -> value.plus(Pair("option", "my option")) }
        expectationDetails["option"] = "my option"

        assertEquals(expectation, actualValue)

        form.transformValue { value -> value.plus(Pair("first_name", "test")) }
        expectation["first_name"] = "test"

        assertEquals(expectation, actualValue)

        collectJob.cancel()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun formGroupValidation() = runTest {
        val detailsForm = FormGroupBuilder()
            .withControl("option", textControl("", arrayOf(ValidatorRequired())))
            .build()

        val firstNameControl = textControl("", arrayOf(
            ValidatorRequired(),
            ValidatorPattern("^[0-9a-z]$"),
        ))

        val form = FormGroupBuilder()
            .withControl("first_name", firstNameControl)
            .withControl("last_name", textControl())
            .withControl("details", detailsForm)
            .build()

        var errors: Errors = listOf()

        val collectJob = launch(UnconfinedTestDispatcher()) {
            form.state.collect { state ->
                errors = state.errors
            }
        }

        val controlErrors1 = arrayOf(
            ValidationError("pattern", "wrong value"),
            ValidationError("required", "value required")
        )

        val formErrors1 = arrayOf(
            ValidationError("pattern", "wrong value", Path("first_name")),
            ValidationError("required", "value required", Path("first_name"))
        )

        val formErrors2 = formErrors1.plus(
            ValidationError("required", "value required", Path("details", "option"))
        )

        assertArrayEquals(controlErrors1, firstNameControl.validate().toTypedArray())
        assertArrayEquals(formErrors1, errors.toTypedArray())

        assertArrayEquals(formErrors2, form.validate().toTypedArray())
        assertArrayEquals(formErrors2, errors.toTypedArray())

        firstNameControl.transformValue{ "héllo!>" }

        val controlErrors2 = arrayOf(
            ValidationError("pattern", "wrong value"),
        )

        val formErrors3 = arrayOf(
            ValidationError("pattern", "wrong value", Path("first_name")),
        )

        val formErrors4 = formErrors3.plus(
            ValidationError("required", "value required", Path("details", "option"))
        )

        form.clearErrors()

        assertArrayEquals(controlErrors2, firstNameControl.validate().toTypedArray())
        assertArrayEquals(formErrors3, errors.toTypedArray())

        assertArrayEquals(formErrors4, form.validate().toTypedArray())
        assertArrayEquals(formErrors4, errors.toTypedArray())

        collectJob.cancel()
    }
}