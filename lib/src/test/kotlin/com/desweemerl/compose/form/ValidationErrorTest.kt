package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidator

fun assertValidationErrors(expectation: ValidationErrors, actual: ValidationErrors) {
    if (!expectation.all { error -> actual.hasErrorOfType(error.type, error.path) }) {
        throw AssertionError("Assertion failed: expectation=${expectation} actual=${actual}")
    }
}

class DummyValidator(
    override val message: String
) : FormValidator<String>(message = "dummy error") {
    override suspend fun validate(state: IFormState<String>): ValidationError? =
        if (state.value == "error") {
            ValidationError("dummy", message)
        } else {
            null
        }
}

class ValidationErrorTest {
// TODO
}