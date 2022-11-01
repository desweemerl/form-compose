package com.desweemerl.compose.form

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before

open class FormControlTest<V>(protected val control: IFormControl<V>) {
    @ExperimentalCoroutinesApi
    private val scope = CoroutineScope(UnconfinedTestDispatcher())

    protected var state: IFormState<V>? = null
    private lateinit var job: Job

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun prepareTest() {
        state = null
        job = scope.launch {
            control.registerCallback { state = it }
        }
    }

    @After
    fun finalizeTest() {
        job.cancel()
    }
}