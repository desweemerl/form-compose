package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


typealias FormStateCallback<V> = Callback<FormState<V>>

interface IFormControl<V> {
    val validators: FormValidators<V>
    val state: FormState<V>

    suspend fun registerCallback(callback: FormStateCallback<V>)
    suspend fun unregisterCallback(callback: FormStateCallback<V>)

    suspend fun transformValue(transform: (value: V) -> V): FormState<V>
    suspend fun validate(): FormState<V>
}

abstract class AbstractFormControl<V>(
    initialState: FormState<V>
) : IFormControl<V> {
    private val stateMutex = Mutex()
    private var _state = initialState
    override val state: FormState<V>
        get() = _state

    private val callbacks = Callbacks<FormState<V>>()

    override suspend fun registerCallback(callback: FormStateCallback<V>) =
        callbacks.register(callback)

    override suspend fun unregisterCallback(callback: FormStateCallback<V>) =
        callbacks.unregister(callback)

    internal suspend fun updateState(transform: (state: FormState<V>) -> FormState<V>): FormState<V> =
        stateMutex.withLock {
            _state = transform(state)
            broadcastState(state)
            state
        }

    private suspend fun broadcastState(state: FormState<V>) {
        callbacks.broadcast(state)
    }
}

class FormControl<V>(
    initialState: FormState<V>,
    override val validators: FormValidators<V>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
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
                    .markAsValidating(validationRequested)
                    .requestValidation(validationRequested)
            }

            val errors = mutableListOf<ValidationError>()

            try {
                val mutex = Mutex()
                // Execute all validations at same time
                validators.map { validator ->
                    scope.launch {
                        validator.validate(initialState)?.let { error ->
                            mutex.withLock {
                                errors.add(error)
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
                        .requestValidation(
                            state.validationRequested && ex is CancellationException
                        )
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

fun textControl(initialValue: String = "", validators: FormValidators<String> = arrayOf())
        : FormControl<String> =
    FormControl(FormState(initialValue), validators)
