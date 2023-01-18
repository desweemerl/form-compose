package com.desweemerl.compose.form

interface IFormState<V> {
    val value: V
    val errors: Errors
    //val validators: Validators<IFormState<V>>
    val dirty: Boolean
    val touched: Boolean
    val validating: Boolean

    fun withValue(newValue: V): IFormState<V>
    fun withErrors(newErrors: Errors): IFormState<V>
    fun clearErrors(): IFormState<V>
    fun markAsValidating(newValidating: Boolean = true): IFormState<V>
    fun markAsTouched(): IFormState<V>
    fun markAsDirty(): IFormState<V>
/*
    suspend fun validate(options: ValidatorOptions) =
        validators.foldRight(this) { validator, state ->
            val error = validator.validate(this@IFormState, options)
            if (error == null) state else this.withErrors(this.errors.plus(error))
        }*/
}

class FormState<V>(
    override val value: V,
    override val errors: Errors = listOf(),
    override val dirty: Boolean = false,
    override val touched: Boolean = false,
    //override val validators: Validators<IFormState<V>> = arrayOf(),
    override val validating: Boolean = false,
) : IFormState<V> {
    override fun withValue(newValue: V): IFormState<V> = FormState(
        value = newValue,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = validating,
    )

    override fun withErrors(newErrors: Errors): IFormState<V> = FormState(
        value = value,
        errors = newErrors,
        dirty = dirty,
        touched = touched,
        validating = validating,
    )

    override fun markAsValidating(newValidating: Boolean): IFormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = newValidating,
    )

    override fun markAsTouched(): IFormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = true,
        validating = validating,
    )

    override fun markAsDirty(): IFormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = true,
        touched = touched,
        validating = validating,
    )

    override fun clearErrors(): IFormState<V> = withErrors(listOf())
}
