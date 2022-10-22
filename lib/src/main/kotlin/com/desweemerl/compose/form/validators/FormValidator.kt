package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.*

interface IFormValidatorOptions {
    val force: Boolean
}

class FormValidatorOptions(override val force: Boolean = true) : IFormValidatorOptions

typealias IFormValidator<V> = IValidator<IFormState<V>, IFormValidatorOptions>
typealias FormValidators<V> = Array<IFormValidator<V>>

abstract class FormValidator<V>(
    override val message: String,
) : BaseValidator<IFormState<V>, IFormValidatorOptions>(message) {
    fun applyWhen(dirty: Boolean = true, touched: Boolean = true):
            IFormValidator<V> =
        object : IFormValidator<V> {
            override suspend fun validate(
                state: IFormState<V>,
                options: IFormValidatorOptions,
            ): IValidationError? =
                if ((dirty && state.dirty) || (touched && state.touched)) {
                    validate(state, options)
                } else {
                    null
                }
        }
}