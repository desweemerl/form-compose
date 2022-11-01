package com.desweemerl.compose.form

interface IValidator<S> {
    suspend fun validate(state: S): ValidationError?
}

abstract class BaseValidator<S>(
    open val message: String,
) : IValidator<S>


