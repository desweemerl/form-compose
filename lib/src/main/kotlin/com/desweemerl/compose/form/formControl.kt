package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class FormControl<V>(
    initialState: FormState<V>,
    override val validators: FormValidators<V>,
    private val scope: CoroutineScope,
) : AbstractFormControl<V>(initialState) {
    private var transformJob: Job? = null
    private var validationJob: Job? = null
    private var updateJob: Job? = null

    override suspend fun transformValue(transform: (value: V) -> V): FormState<V> {
        transformJob?.cancel()
        transformJob = scope.launch {
            updateState { state -> state.withValue(transform(state.value)) }
            liveValidate()
        }
        transformJob?.join()

        return state
    }

    override suspend fun validate(): FormState<V> =
        liveValidate(true)

    private suspend fun liveValidate(validationRequested: Boolean = false): FormState<V> {
        validationJob?.cancel()
        validationJob = scope.launch {
            val initialState = updateState { state ->
                state
                    .markAsValidating()
                    .requestValidation(validationRequested)
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
                    state
                        .withErrors(errors)
                        .markAsValidating(false)
                        .requestValidation(false)
                }
            } catch (ex: Exception) {
                updateState { state ->
                    state
                        .markAsValidating(false)
                        .requestValidation(false)
                }
                throw ex
            }
        }
        validationJob?.join()

        return state
    }

    fun update(newState: FormState<V>) {
        updateJob?.cancel()
        transformJob?.cancel()

        updateJob = scope.launch {
            updateState { newState }
            liveValidate()
        }
    }
}

fun textControl(
    initialValue: String = "",
    validators: FormValidators<String> = arrayOf(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : IFormControl<String> =
    FormControl(FormState(initialValue), validators, scope)