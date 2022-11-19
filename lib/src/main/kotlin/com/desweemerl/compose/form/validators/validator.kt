package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.ValidationErrors

interface Validator<in S> {
    suspend fun validate(state: S): ValidationErrors?
}

typealias Validators<S> = Array<Validator<S>>

abstract class AbstractValidator<S>(open val message: String) : Validator<S>