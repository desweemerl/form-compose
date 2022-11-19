package com.desweemerl.compose.form.controls

import com.desweemerl.compose.form.*
import com.desweemerl.compose.form.validators.Validators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FormFieldControl<V>(
    initialState: FormFieldState<V>,
    override val validators: Validators<FormFieldState<V>>,
    private val scope: CoroutineScope,
) : AbstractFormControl<FormFieldState<V>, V>(initialState) {
    private var transformJob: Job? = null
    private var validationJob: Job? = null

    override suspend fun transform(transformer: (state: FormFieldState<V>) -> FormFieldState<V>): FormFieldState<V> {
        transformJob?.cancel()
        transformJob = scope.launch {
            updateState(transformer = transformer)
            liveValidate()
        }
        transformJob?.join()

        return state
    }

    override suspend fun validate(): FormFieldState<V> =
        liveValidate(true)

    private suspend fun liveValidate(validationRequested: Boolean = false): FormFieldState<V> {
        validationJob?.cancel()
        validationJob = scope.launch {
            val initialState = updateState { state ->
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

                updateState { state ->
                    state.copy(
                        validating = false,
                        validationRequested = false,
                        errors = errors
                    )
                }
            } catch (ex: Exception) {
                updateState { state ->
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