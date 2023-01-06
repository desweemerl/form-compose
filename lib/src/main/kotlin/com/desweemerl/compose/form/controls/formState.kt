package com.desweemerl.compose.form.controls


import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.ValidationErrors
import com.desweemerl.compose.form.matches

interface FormState<V> {
    val value: V
    val errors: ValidationErrors
    val dirty: Boolean
    val touched: Boolean
    val enabled: Boolean
    val validating: Boolean
    val validationRequested: Boolean

    fun setValue(transformer: Transformer<V>): FormState<V>
    fun markAsTouched(touched: Boolean = true): FormState<V>
    fun markAsDirty(dirty: Boolean = true): FormState<V>
    fun enable(enabled: Boolean = true): FormState<V>

    fun matches(other: FormState<V>?): Boolean =
        if (other == null) {
            true
        } else {
            value == other.value
                    && errors.matches(other.errors)
                    && dirty == other.dirty
                    && touched == other.touched
                    && enabled == other.enabled
                    && validating == other.validating
                    && validationRequested == other.validationRequested
        }
}