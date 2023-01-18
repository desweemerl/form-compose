package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.*

typealias IFormValidator<V> = IValidator<FormState<V>>
typealias FormValidators<V> = Array<IFormValidator<V>>

abstract class FormValidator<V>(
    override val message: String,
) : BaseValidator<FormState<V>>(message) {
    fun applyWhen(dirty: Boolean = true):
            IFormValidator<V> =
        object : IFormValidator<V> {
            override suspend fun validate(state: FormState<V>): ValidationError? =
                if (dirty && state.dirty) {
                    validate(state)
                } else {
                    null
                }
        }
}