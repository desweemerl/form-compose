package com.desweemerl.compose.form

interface IValidator<S, O> {
    suspend fun validate(state: S, options: O): IValidationError?
}

typealias Validators<S, O> = Array<IValidator<S, O>>

suspend fun <V, O> Validators<IFormState<V>, O>.validate(state: IFormState<V>, options: O): IFormState<V> =
    foldRight(state) { validator, nextState ->
        val error = validator.validate(nextState, options)
        if (error == null) nextState else nextState.withErrors(nextState.errors.plus(error))
    }

abstract class BaseValidator<S, O>(
    open val message: String,
) : IValidator<S, O>


