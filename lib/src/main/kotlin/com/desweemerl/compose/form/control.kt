package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidators
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


typealias FormStateCallback<V> = Callback<IFormState<V>>

interface IFormControl<V> {
    val validators: FormValidators<V>
    val state: IFormState<V>
    val stateFlow: StateFlow<IFormState<V>>

    suspend fun registerCallback(callback: FormStateCallback<V>)
    suspend fun unregisterCallback(callback: FormStateCallback<V>)

    suspend fun transformValue(transform: (value: V) -> V): IFormState<V>
    suspend fun validate(): IFormState<V>
}

abstract class AbstractFormControl<V>(
    initialState: IFormState<V>
) : IFormControl<V> {
    private val stateMutex = Mutex()

    override val state: IFormState<V>
        get() = stateFlow.value

    private val _stateFlow = MutableStateFlow(initialState)
    override val stateFlow = _stateFlow.asStateFlow()

    private val callbacks = Callbacks<IFormState<V>>()

    override suspend fun registerCallback(callback: FormStateCallback<V>) =
        callbacks.register(callback)

    override suspend fun unregisterCallback(callback: FormStateCallback<V>) =
        callbacks.unregister(callback)

    internal suspend fun updateState(transform: (state: IFormState<V>) -> IFormState<V>): IFormState<V> =
        stateMutex.withLock {
            _stateFlow.getAndUpdate { state -> transform(state) }
            broadcastState(state)
            state
        }

    private suspend fun broadcastState(state: IFormState<V>) {
        callbacks.broadcast(state)
        _stateFlow.emit(state)
    }
}

class FormControl<V>(
    initialState: IFormState<V>,
    override val validators: FormValidators<V>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : AbstractFormControl<V>(initialState) {
    private var transformJob: Job? = null
    private var validationJob: Job? = null
    private var updateJob: Job? = null

    override suspend fun transformValue(transform: (value: V) -> V): IFormState<V> {
        transformJob?.cancel()
        transformJob = scope.launch {
            updateState { state -> state.withValue(transform(state.value)) }
            liveValidate()
        }
        transformJob?.join()

        return state
    }

    override suspend fun validate(): IFormState<V> =
        liveValidate(true)

    private suspend fun liveValidate(validationRequested: Boolean = false): IFormState<V> {
        validationJob?.cancel()
        validationJob = scope.launch {
            val initialState = updateState { state ->
                state
                    .clearErrors()
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
                        .clearErrors()
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


    fun update(newState: IFormState<V>) {
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
