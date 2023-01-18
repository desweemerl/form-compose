package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.FormState
import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.ValidationErrors

private const val defaultMessage = "wrong value"

fun ValidatorPattern(regex: String, message: String = defaultMessage): ValidatorPattern =
    ValidatorPattern(regex.toRegex(), message)

class ValidatorPattern(
    val regex: Regex,
    override val message: String = defaultMessage,
) : FormValidator<String>(message = message) {
    override suspend fun validate(state: FormState<String>): ValidationErrors? =
        if (state.value.isNotEmpty() && !regex.matches(state.value)) {
            listOf(ValidationError("pattern", message))
        } else {
            null
        }
}