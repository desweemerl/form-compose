package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.*

typealias IFormValidator<V> = IValidator<IFormState<V>>
typealias FormValidators<V> = Array<IFormValidator<V>>

abstract class FormValidator<V>(open val message: String) : IValidator<IFormState<V>>