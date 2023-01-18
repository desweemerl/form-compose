package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.*

typealias IFormValidator<V> = IValidator<FormState<V>>
typealias FormValidators<V> = Array<IFormValidator<V>>

abstract class FormValidator<V>(
    override val message: String,
) : BaseValidator<FormState<V>>(message)