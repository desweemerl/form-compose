package com.desweemerl.compose.form.controls

import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.ValidationErrors

class FormGroupState(
    val controls: Map<String, Control<FormState<Any>>>,
    val formValue: Map<String, Any> = mapOf(),
    private val formErrors: ValidationErrors = listOf(),
    val formTouched: Boolean? = null,
    val formDirty: Boolean? = null,
    private var controlsDirty: Boolean = true,
    override val validating: Boolean = false,
    override val validationRequested: Boolean = false,
) : FormState<Map<String, Any>> {
    private var _errors: ValidationErrors? = null

    override val value: Map<String, Any>
        get() = formValue.plus(controls.getValues())

    override val errors: ValidationErrors
        get() {
            if (_errors == null || controlsDirty) {
                _errors = controls.getErrors()
                controlsDirty = false
            }

            return _errors!!.plus(formErrors)
        }

    override val touched: Boolean
        get() = controls.touched()

    override val dirty: Boolean
        get() = controls.dirty()

    override fun toString(): String = """
        FormGroupState{value=$value errors=$errors
        dirty=$dirty touched=$touched
        validating=$validating validationRequested=$validationRequested
        formValue=$formValue formErrors=$formErrors controlsDirty=$controlsDirty}""".trimIndent()

    override fun setValue(transformer: Transformer<Map<String, Any>>): FormGroupState =
        copy(formValue = transformer(value))

    override fun markAsTouched(touched: Boolean): FormGroupState =
        copy(formTouched = touched)

    override fun markAsDirty(dirty: Boolean): FormGroupState =
        copy(formDirty = dirty)

    fun copy(
        formValue: Map<String, Any> = this.formValue,
        formErrors: ValidationErrors = this.formErrors,
        formTouched: Boolean? = this.formTouched,
        formDirty: Boolean? = this.formDirty,
        controlsDirty: Boolean = this.controlsDirty,
        validating: Boolean = this.validating,
        validationRequested: Boolean = this.validationRequested,
    ): FormGroupState =
        FormGroupState(
            controls = controls,
            formValue = formValue,
            formErrors = formErrors,
            formTouched = formTouched,
            formDirty = formDirty,
            controlsDirty = controlsDirty,
            validating = validating,
            validationRequested = validationRequested,
        )
}
