package com.desweemerl.compose.form

import com.desweemerl.compose.form.controls.textControl
import com.desweemerl.compose.form.validators.ValidatorRequired
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

val requiredError = listOf(ValidationError("required", "value required"))

class FormFieldControlSetValueTest :
    FormFieldControlTest<String>(
        textControl(initialValue = "initial", validators = arrayOf(ValidatorRequired()))
    ) {

    @Test
    fun `When control is initialized expect state has the initial value`() {
        assertEquals("initial", state.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When control is updated expect state has the new value`() =
        runTest {
            assertEquals(
                "initial_next",
                control.setValue { value -> "${value}_next" }.value
            )
            assertEquals("initial_next", state.value)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When control is updated with wrong value expect result and state have error`() =
        runTest {
            assertMatchErrors(
                requiredError,
                control.setValue { "" }.errors
            )
            assertMatchErrors(requiredError, state.errors)
        }
}

class FormFieldControlValidationTest :
    FormFieldControlTest<String>(
        textControl(validators = arrayOf(ValidatorRequired()))
    ) {

    @Test
    @ExperimentalCoroutinesApi
    fun `When control is initialized with a wrong value expect initial state contains no error`() =
        runTest {
            assertMatchErrors(listOf(), state.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When control is initialized with a wrong value expect validation return error`() =
        runTest {
            assertMatchErrors(requiredError, control.validate().errors)
        }
}