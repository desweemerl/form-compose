package com.desweemerl.compose.form

interface IFormState<V> {
    val value: V
    val errors: ValidationErrors

    fun withValue(newValue: V): IFormState<V>
    fun withErrors(newErrors: ValidationErrors): IFormState<V>

    val dirty: Boolean
    val touched: Boolean
    val validating: Boolean
    val validationRequested: Boolean

    fun markAsValidating(newValidating: Boolean = true): IFormState<V>
    fun markAsTouched(newTouched: Boolean = true): IFormState<V>
    fun markAsDirty(newDirty: Boolean = true): IFormState<V>
    fun requestValidation(requested: Boolean = true): IFormState<V>

    fun matches(other: IFormState<V>?): Boolean =
        if (other == null) {
            true
        } else {
            value == other.value
                    && errors.matches(other.errors)
                    && dirty == other.dirty
                    && touched == other.touched
                    && validating == other.validating
                    && validationRequested == other.validationRequested
        }
}

open class FormState<V>(
    override val value: V,
    override val errors: ValidationErrors = listOf(),
    override val dirty: Boolean = false,
    override val touched: Boolean = false,
    override val validating: Boolean = false,
    override val validationRequested: Boolean = false,
) : IFormState<V> {
    override fun withValue(newValue: V): IFormState<V> =
        FormState<V>(
            value = newValue,
            errors = errors,
            dirty = dirty,
            touched = touched,
            validating = validating,
            validationRequested = validationRequested,
        )

    override fun withErrors(newErrors: ValidationErrors): IFormState<V> =
        FormState(
            value = value,
            errors = newErrors,
            dirty = dirty,
            touched = touched,
            validating = validating,
            validationRequested = validationRequested,
        )

    override fun markAsValidating(newValidating: Boolean): IFormState<V> =
        FormState(
            value = value,
            errors = errors,
            dirty = dirty,
            touched = touched,
            validating = newValidating,
            validationRequested = validationRequested,
        )

    override fun requestValidation(requested: Boolean): IFormState<V> =
        FormState(
            value = value,
            errors = errors,
            dirty = dirty,
            touched = touched,
            validating = validating,
            validationRequested = requested,
        )

    override fun markAsTouched(newTouched: Boolean): IFormState<V> =
        FormState(
            value = value,
            errors = errors,
            dirty = dirty,
            touched = newTouched,
            validating = validating,
            validationRequested = validationRequested,
        )

    override fun markAsDirty(newDirty: Boolean): IFormState<V> =
        FormState(
            value = value,
            errors = errors,
            dirty = true,
            touched = touched,
            validating = validating,
            validationRequested = validationRequested,
        )

    override fun toString(): String = """
        FormState{value=$value errors=$errors
        dirty=$dirty touched=$touched
        validating=$validating validationRequested=$validationRequested}""".trimIndent()
}

