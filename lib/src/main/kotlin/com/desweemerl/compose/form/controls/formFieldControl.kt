package com.desweemerl.compose.form.controls

import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.validators.Validators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FormFieldControl<V>(
    initialState: FormFieldState<V>,
    override val validators: Validators<FormFieldState<V>>,
    private val scope: CoroutineScope,
    private var parentControl: FormGroupControl? = null,
) : AbstractFormControl<FormFieldState<V>, V>(initialState) {
    private var validationJob: Job? = null

    private suspend fun updateAndNotify(transformer: (state: FormFieldState<V>) -> FormFieldState<V>): FormFieldState<V> {
        val newState = updateState(transformer)
        parentControl?.notifyStateChange()
        return newState
    }

    override suspend fun transform(transformer: (state: FormFieldState<V>) -> FormFieldState<V>): FormFieldState<V> {
        updateAndNotify(transformer = transformer)
        liveValidate()

        return state
    }

    override suspend fun validate(): FormFieldState<V> =
        liveValidate(true)

    override fun bind(control: Control<*>) {
        if (parentControl == null) {
            if (control is FormGroupControl) {
                parentControl = control
            } else {
                throw Exception("control must be a FormGroupControl")
            }
        } else {
            throw Exception("control is already bound")
        }
    }

    private suspend fun liveValidate(validationRequested: Boolean = false): FormFieldState<V> {
        validationJob?.cancelAndJoin()
        validationJob = scope.launch {
            val initialState = updateAndNotify { state ->
                state.copy(
                    validating = true,
                    validationRequested = validationRequested
                )
            }

            val errors = mutableListOf<ValidationError>()

            try {
                val mutex = Mutex()
                // Execute all validations at same time
                validators.map { validator ->
                    scope.launch {
                        validator.validate(initialState)?.let { validationErrors ->
                            mutex.withLock {
                                errors.addAll(validationErrors)
                            }
                        }
                    }
                }.joinAll()

                updateAndNotify { state ->
                    state.copy(
                        validating = false,
                        validationRequested = false,
                        errors = errors
                    )
                }
            } catch (ex: Exception) {
                updateAndNotify { state ->
                    state.copy(
                        validating = false,
                        validationRequested = false,
                    )
                }
                throw ex
            }
        }
        validationJob?.join()

        return state
    }
}

fun textControl(
    initialValue: String = "",
    validators: Validators<FormFieldState<String>> = arrayOf(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): FormFieldControl<String> =
    FormFieldControl(FormFieldState(initialValue), validators, scope)