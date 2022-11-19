package com.desweemerl.compose.form.controls

import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.ValidationErrors
import com.desweemerl.compose.form.withEmptyPath

class FormFieldState<V>(
    override val value: V,
    override val errors: ValidationErrors = listOf(),
    override val dirty: Boolean = false,
    override val touched: Boolean = false,
    override val validating: Boolean = false,
    override val validationRequested: Boolean = false,
) : FormState<V> {
    override fun toString(): String = """
        FormFieldState{value=$value errors=$errors
        dirty=$dirty touched=$touched
        validating=$validating validationRequested=$validationRequested}""".trimIndent()

    override fun setValue(transformer: Transformer<V>): FormFieldState<V> =
        copy(value = transformer(value))

    override fun markAsTouched(touched: Boolean): FormState<V> =
        copy(touched = touched)

    override fun markAsDirty(dirty: Boolean): FormState<V> =
        copy(dirty = dirty)

    fun copy(
        value: V = this.value,
        errors: ValidationErrors = this.errors,
        dirty: Boolean = this.dirty,
        touched: Boolean = this.touched,
        validating: Boolean = this.validating,
        validationRequested: Boolean = this.validationRequested,
    ): FormFieldState<V> = FormFieldState(
        value = value,
        errors = errors,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )
}

inline fun <V> mergeErrors(crossinline otherState: () -> FormState<*>)
        : Transformer<FormFieldState<V>> = { state ->
    state.copy(errors = state.errors.plus(otherState().errors.withEmptyPath()))
}

inline fun <V> whenTouched(crossinline transformer: Transformer<FormFieldState<V>>)
        : Transformer<FormFieldState<V>> = { state ->
    if (state.touched) {
        transformer(state)
    } else {
        state
    }
}