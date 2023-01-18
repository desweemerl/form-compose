package com.desweemerl.compose.form

import com.desweemerl.compose.form.controls.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

open class ControlTest<S>(protected open val control: Control<S>) {
    @ExperimentalCoroutinesApi
    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    protected val state: S
    get() = control.state
}

open class FormFieldControlTest<V>(override val control: FormFieldControl<V>) :
    ControlTest<FormFieldState<V>>(control)

open class FormGroupControlTest(override val control: FormGroupControl) :
    ControlTest<FormGroupState>(control) {

    @Suppress("UNCHECKED_CAST")
    fun getTextField(key: String): FormFieldControl<String> =
        control.getControl(key) as FormFieldControl<String>

    @Suppress("UNCHECKED_CAST")
    fun getFormGroupControl(key: String): FormGroupControl =
        control.getControl(key) as FormGroupControl
}