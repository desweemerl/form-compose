package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.ValidationErrors
import com.desweemerl.compose.form.controls.FormState


class ValidatorMin(
    val minValue: Int,
    override val message: String = "must be greater than $minValue",
) : AbstractValidator<FormState<Int?>>(message = message) {
    override suspend fun validate(state: FormState<Int?>): ValidationErrors? =
        state.value?.let { value ->
            if (value < minValue) {
                listOf(ValidationError("min", message))
            } else {
                null
            }
        }
}

class ValidatorMax(
    val maxValue: Int,
    override val message: String = "must be lower than $maxValue",
) : AbstractValidator<FormState<Int?>>(message = message) {
    override suspend fun validate(state: FormState<Int?>): ValidationErrors? =
        state.value?.let { value ->
            if (value > maxValue) {
                listOf(ValidationError("max", message))
            } else {
                null
            }
        }
}