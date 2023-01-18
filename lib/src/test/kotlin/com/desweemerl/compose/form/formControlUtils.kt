package com.desweemerl.compose.form

import com.desweemerl.compose.form.controls.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before

open class ControlTest<S>(protected open val control: Control<S>) {
    @ExperimentalCoroutinesApi
    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    protected var state: S? = null

    @Before
    fun prepareTest() {
        runBlocking {
            control.registerCallback { state = it }
        }
    }
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