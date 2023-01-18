package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.controls.FormState
import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.ValidationErrors

class ValidatorRequired(
    override val message: String = "required"
) : AbstractValidator<FormState<String>>(message = message) {
    override suspend fun validate(state: FormState<String>): ValidationErrors? =
        if (state.value.trim().isEmpty()) {
            listOf(ValidationError("required", message))
        } else {
            null
        }
}