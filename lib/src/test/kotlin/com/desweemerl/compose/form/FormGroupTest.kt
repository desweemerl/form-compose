package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.ValidatorPattern
import com.desweemerl.compose.form.validators.ValidatorRequired
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test


class FormGroupTransformValueTest :
    FormControlTest<Map<String, Any>>(
        FormGroupBuilder()
            .withControl("first_name", textControl(""))
            .withControl("last_name", textControl())
            .build()
    ) {

    @Test
    fun `When form is initialized expect state has the initial value`() {
        val expectation = mapOf(
            Pair("first_name", ""),
            Pair("last_name", ""),
        )

        assertMatch(expectation, control.state.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a control is updated expect state and callback have the new value`() = runTest {
        val expectation = mutableMapOf(
            Pair("first_name", "test"),
            Pair("last_name", ""),
        )

        (control as FormGroupControl).getControl("first_name")?.transformValue { "test" }

        assertMatch(expectation, control.state.value)
        assertMatch(expectation, state?.value)

        expectation["first_name"] = "test_bis"
        control.transformValue { value ->
            value.plus(
                Pair(
                    "first_name",
                    "${value["first_name"]}_bis"
                )
            )
        }

        assertMatch(expectation, control.state.value)
        assertMatch(expectation, state?.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When form is updated with an additional parameter expect state and callback include that parameter`() =
        runTest {
            val expectation = mapOf(
                Pair("first_name", ""),
                Pair("last_name", ""),
                Pair("new_field", "new_value"),
            )

            control.transformValue { value -> value.plus(Pair("new_field", "new_value")) }

            assertMatch(expectation, control.state.value)
            assertMatch(expectation, state?.value)
        }
}


class NestedFormGroupTest :
    FormControlTest<Map<String, Any>>(
        FormGroupBuilder()
            .withControl("first_name", textControl(""))
            .withControl("last_name", textControl())
            .withControl(
                "details", FormGroupBuilder()
                    .withControl("option", textControl(""))
                    .build()
            )
            .build()
    ) {

    @Test
    fun `When form is initialized expect state has the initial value`() {
        val expectation = mapOf(
            Pair("first_name", ""),
            Pair("last_name", ""),
            Pair("details", mapOf(Pair("option", ""))),
        )

        assertMatch(expectation, control.state.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When the child form is updated expect state and callback have the new value`() = runTest {
        val expectation = mapOf(
            Pair("first_name", ""),
            Pair("last_name", ""),
            Pair("details", mapOf(Pair("option", "my option"))),
        )

        ((control as FormGroupControl).getControl("details") as FormGroupControl)
            .transformValue { value -> value.plus(Pair("option", "my option")) }

        assertMatch(expectation, control.state.value)
        assertMatch(expectation, state?.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When the parent form is updated expect state and callback have the new value`() = runTest {
        val expectation = mapOf(
            Pair("first_name", "test"),
            Pair("last_name", ""),
            Pair("details", mapOf(Pair("option", ""))),
        )

        (control as FormGroupControl).transformValue { value ->
            value.plus(
                Pair(
                    "first_name",
                    "test"
                )
            )
        }

        assertMatch(expectation, control.state.value)
        assertMatch(expectation, state?.value)
    }
}

class FormGroupValidationTest :
    FormControlTest<Map<String, Any>>(
        FormGroupBuilder()
            .withControl(
                "first_name", textControl(
                    "", arrayOf(
                        ValidatorRequired(),
                        ValidatorPattern("^[0-9a-z]$"),
                    )
                )
            )
            .withControl("last_name", textControl())
            .withControl(
                "details", FormGroupBuilder()
                    .withControl("option", textControl("", arrayOf(ValidatorRequired())))
                    .build()
            )
            .build()
    ) {

    private val firstNameControl: IFormControl<String>
        @Suppress("UNCHECKED_CAST")
        get() = (control as FormGroupControl).getControl("first_name") as FormControl<String>

    @Test
    @ExperimentalCoroutinesApi
    fun `When a control has errors and validation is done on that control expect states on form and control have errors of the control`() =
        runTest {
            val controlErrors = listOf(
                ValidationError("required", "value required"),
            )

            val formErrors = listOf(
                ValidationError("required", "value required", Path("first_name")),
            )

            assertMatchErrors(controlErrors, firstNameControl.validate().errors)
            assertMatchErrors(formErrors, control.state.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a control has errors and validation is done on the form expect states on form and control have all errors`() =
        runTest {
            val formErrors = listOf(
                ValidationError("required", "value required", Path("first_name")),
                ValidationError("required", "value required", Path("details", "option")),
            )

            assertMatchErrors(formErrors, control.validate().errors)
            assertMatchErrors(formErrors, state?.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a control is updated with wrong value expect states on form and control have errors of the control`() =
        runTest {
            val controlErrors = listOf(
                ValidationError("pattern", "wrong value"),
            )

            val formErrors = listOf(
                ValidationError("pattern", "wrong value", Path("first_name")),
            )

            assertMatchErrors(controlErrors, firstNameControl.transformValue { "héllo!>" }.errors)
            assertMatchErrors(formErrors, control.state.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a control is updated with wrong value and validation is done on the form expect form state has all errors`() =
        runTest {
            val formErrors = listOf(
                ValidationError("pattern", "wrong value", Path("first_name")),
                ValidationError("required", "value required", Path("details", "option")),
            )

            firstNameControl.transformValue { "héllo!>" }
            assertMatchErrors(formErrors, control.validate().errors)
            assertMatchErrors(formErrors, control.state.errors)
        }
}