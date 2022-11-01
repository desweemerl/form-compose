package com.desweemerl.compose.form

import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.validators.ValidatorRequired
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

val requiredError = listOf(ValidationError("required", "value required"))

class FormControlTransformValueTest :
    FormControlTest<String>(
        textControl(initialValue = "initial", validators = arrayOf(ValidatorRequired()))
    ) {

    @Test
    fun `When control is initialized expect state has the initial value`() {
        assertEquals("initial", control.state.value)
    }

    @Test
    fun `When control is initialized expect callback value is null`() {
        assertEquals(null, state?.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When control is updated expect state and callback have the new value`() =
        runTest {
            assertEquals(
                "initial_next",
                control.transformValue { value -> "${value}_next" }.value
            )
            assertEquals("initial_next", control.state.value)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When control is updated with wrong value expect result and state have error`() =
        runTest {
            assertMatchErrors(requiredError, control.transformValue { "" }.errors)
            assertMatchErrors(requiredError, control.state.errors)
            assertMatchErrors(requiredError, state?.errors)
        }
}

class FormControlValidationTest :
    FormControlTest<String>(
        textControl(validators = arrayOf(ValidatorRequired()))
    ) {

    @Test
    @ExperimentalCoroutinesApi
    fun `When control is initialized with a wrong value expect initial state contains no error`() =
        runTest {
            assertMatchErrors(listOf(), control.state.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When control is initialized with a wrong value expect validation return error`() =
        runTest {
            assertMatchErrors(requiredError, control.validate().errors)
        }
}