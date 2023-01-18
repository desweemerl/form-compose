package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.IFormState
import com.desweemerl.compose.form.IValidationError
import com.desweemerl.compose.form.ValidationError

private const val defaultMessage = "wrong value"

fun ValidatorPattern(regex: String, message: String = defaultMessage): ValidatorPattern =
    ValidatorPattern(regex.toRegex(), message)

class ValidatorPattern(
    val regex: Regex,
    override val message: String = defaultMessage,
) : FormValidator<String>(message = message) {
    override suspend fun validate(
        state: IFormState<String>,
        options: IFormValidatorOptions,
    ): IValidationError? =
        if (!regex.matches(state.value)) {
            ValidationError("pattern", message)
        } else {
            null
        }
}