package com.desweemerl.compose.form

interface IFormState<V> {
    val value: V
    val errors: ValidationErrors
    val dirty: Boolean
    val touched: Boolean
    val validating: Boolean
    val validationRequested: Boolean

    fun withValue(newValue: V): IFormState<V>
    fun withErrors(newErrors: ValidationErrors): IFormState<V>
    fun clearErrors(): IFormState<V>
    fun markAsValidating(newValidating: Boolean = true): IFormState<V>
    fun requestValidation(requested: Boolean = true): IFormState<V>
    fun markAsTouched(): IFormState<V>
    fun markAsDirty(): IFormState<V>
}

class FormState<V>(
    override val value: V,
    override val errors: ValidationErrors = listOf(),
    override val dirty: Boolean = false,
    override val touched: Boolean = false,
    override val validating: Boolean = false,
    override val validationRequested: Boolean = false,
) : IFormState<V> {
    override fun withValue(newValue: V): IFormState<V> = FormState(
        value = newValue,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    override fun withErrors(newErrors: ValidationErrors): IFormState<V> = FormState(
        value = value,
        errors = newErrors,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    override fun markAsValidating(newValidating: Boolean): IFormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = newValidating,
        validationRequested = validationRequested,
    )

    override fun requestValidation(requested: Boolean): IFormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = requested,
    )

    override fun markAsTouched(): IFormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = true,
        validating = validating,
        validationRequested = validationRequested,
    )

    override fun markAsDirty(): IFormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = true,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    override fun clearErrors(): IFormState<V> = withErrors(listOf())

    override fun toString(): String ="""
        FormState{value=$value errors=$errors
        dirty=$dirty touched=$touched
        validating=$validating validationRequested=$validationRequested}""".trimIndent()
}
