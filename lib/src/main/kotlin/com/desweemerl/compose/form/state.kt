package com.desweemerl.compose.form

class FormState<V>(
    val value: V,
    val errors: ValidationErrors = listOf(),
    val dirty: Boolean = false,
    val touched: Boolean = false,
    val validating: Boolean = false,
    val validationRequested: Boolean = false,
) {
    fun withValue(newValue: V): FormState<V> = FormState(
        value = newValue,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    fun withErrors(newErrors: ValidationErrors): FormState<V> = FormState(
        value = value,
        errors = newErrors,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    fun markAsValidating(newValidating: Boolean = true): FormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = newValidating,
        validationRequested = validationRequested,
    )

    fun requestValidation(requested: Boolean = true): FormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = requested,
    )

    fun markAsTouched(): FormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = true,
        validating = validating,
        validationRequested = validationRequested,
    )

    fun markAsDirty(): FormState<V> = FormState(
        value = value,
        errors = errors,
        dirty = true,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    fun matches(other: FormState<V>?): Boolean =
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

    override fun toString(): String = """
        FormState{value=$value errors=$errors
        dirty=$dirty touched=$touched
        validating=$validating validationRequested=$validationRequested}""".trimIndent()
}
