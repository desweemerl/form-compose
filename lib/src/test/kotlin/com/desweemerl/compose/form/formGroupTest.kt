package com.desweemerl.compose.form

import com.desweemerl.compose.form.controls.FormGroupBuilder
import com.desweemerl.compose.form.controls.FormGroupState
import com.desweemerl.compose.form.controls.textControl
import com.desweemerl.compose.form.validators.Validator
import com.desweemerl.compose.form.validators.ValidatorPattern
import com.desweemerl.compose.form.validators.ValidatorRequired
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test


class FormGroupSetValueTest : FormGroupControlTest(
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
    fun `When a control is updated expect state has the new value`() = runTest {
        val expectation = mutableMapOf(
            Pair("first_name", "test"),
            Pair("last_name", ""),
        )

        getTextField("first_name").setValue { "test" }

        assertMatch(expectation, state.value)

        expectation["first_name"] = "test_bis"
        control.setValue { value ->
            value.plus(
                Pair(
                    "first_name",
                    "${value["first_name"]}_bis"
                )
            )
        }

        assertMatch(expectation, state.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When form is updated with an additional parameter expect state includes that parameter`() =
        runTest {
            val expectation = mapOf(
                Pair("first_name", ""),
                Pair("last_name", ""),
                Pair("new_field", "new_value"),
            )

            control.setValue { value -> value.plus(Pair("new_field", "new_value")) }
            assertMatch(expectation, state.value)
        }
}


class NestedFormGroupTest : FormGroupControlTest(
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

        assertMatch(expectation, state.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a disabled form is initialized expect state is empty`() = runTest {
        control.enable(false)

        assertMatch(mapOf(), state.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When the child form is updated expect state has the new value`() = runTest {
        val expectation = mapOf(
            Pair("first_name", ""),
            Pair("last_name", ""),
            Pair("details", mapOf(Pair("option", "my option"))),
        )

        getFormGroupControl("details")
            .setValue { value -> value.plus(Pair("option", "my option")) }

        assertMatch(expectation, state.value)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When the parent form is updated expect state has the new value`() = runTest {
        val expectation = mapOf(
            Pair("first_name", "test"),
            Pair("last_name", ""),
            Pair("details", mapOf(Pair("option", ""))),
        )

        control.setValue { value ->
            value.plus(
                Pair(
                    "first_name",
                    "test"
                )
            )
        }

        assertMatch(expectation, state.value)
    }
}

class FormGroupValidationTest : FormGroupControlTest(
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

            assertMatchErrors(controlErrors, getTextField("first_name").validate().errors)
            assertMatchErrors(formErrors, state.errors)
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
            assertMatchErrors(formErrors, state.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a disabled control has errors and validation is done on the form expect states on form and control have no errors`() =
        runTest {
            control.enable(false)

            assertMatchErrors(listOf(), control.validate().errors)
            assertMatchErrors(listOf(), state.errors)
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

            assertMatchErrors(
                controlErrors,
                getTextField("first_name").setValue { "héllo!>" }.errors
            )
            assertMatchErrors(formErrors, state.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a disabled control is updated with wrong value expect states on form and control have no errors`() =
        runTest {
            control.enable(false)
            
            assertMatchErrors(
                listOf(),
                getTextField("first_name").setValue { "héllo!>" }.errors
            )
            assertMatchErrors(listOf(), state.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a control is updated with wrong value and validation is done on the form expect form state has all errors`() =
        runTest {
            val formErrors = listOf(
                ValidationError("pattern", "wrong value", Path("first_name")),
                ValidationError("required", "value required", Path("details", "option")),
            )

            getTextField("first_name").setValue { "héllo!>" }
            assertMatchErrors(formErrors, control.validate().errors)
            assertMatchErrors(formErrors, state.errors)
        }

    @Test
    @ExperimentalCoroutinesApi
    fun `When a disabled control is updated with wrong value and validation is done on the form expect form state has no errors`() =
        runTest {
            control.enable(false)
            getTextField("first_name").setValue { "héllo!>" }
            assertMatchErrors(listOf(), control.validate().errors)
            assertMatchErrors(listOf(), state.errors)
        }
}

class FormGroupValidationRequestedTest : FormGroupControlTest(
    FormGroupBuilder()
        .withControl(
            "first_name", textControl(
                "", arrayOf(
                    ValidatorRequired(),
                )
            )
        )
        .withControl("last_name", textControl())
        .withValidator(object : Validator<FormGroupState> {
            override suspend fun validate(state: FormGroupState): ValidationErrors? {
                val firstName = state.value["first_name"] as? String ?: ""
                if (state.validationRequested && firstName != "test") {
                    return listOf(ValidationError("custom", "value is not equal to 'test'"))
                }

                return null
            }
        })
        .build()
) {
    @Test
    @ExperimentalCoroutinesApi
    fun `When a validation has been requested expect flag validationRequested dispatched to validators`() =
        runTest {
            val formErrors = listOf(
                ValidationError("required", "value required", Path("first_name")),
             )
            val globalFormErrors = listOf(
                ValidationError("custom", "value is not equal to 'test'"),
            )

            getTextField("first_name").setValue { "" }
            assertMatchErrors(formErrors, state.errors)
            assertMatchErrors(formErrors + globalFormErrors, control.validate().errors)
            getTextField("first_name").markAsTouched()
            assertMatchErrors(formErrors, state.errors)
        }
}