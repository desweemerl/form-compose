package com.desweemerl.compose.form

import kotlinx.coroutines.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before

open class FormControlTest<V>(protected open val control: IFormControl<V>) {
    @ExperimentalCoroutinesApi
    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    protected var state: FormState<V>? = null

    @Before
    fun prepareTest() {
        runBlocking {
            control.registerCallback { state = it }
        }
    }
}

open class FormGroupTest(override val control: IFormGroupControl)
    : FormControlTest<Map<String, Any>>(control) {
}