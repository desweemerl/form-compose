package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.*

class ValidatorRequired(
    override val message: String = "value required"
) : FormValidator<String>(message = message) {
    override suspend fun validate(state: FormState<String>): ValidationError? =
        if (state.value.trim().isEmpty()) {
            ValidationError("required", message)
        } else {
            null
        }
}