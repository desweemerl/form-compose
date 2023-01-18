package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidators
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
    initialState: FormState<V>,
    initialCallbacks: List<Callback<FormState<V>>>? = null,
) : IFormControl<V> {
    private val stateMutex = Mutex()
    internal var _state = initialState

    override val state: FormState<V>
        get() = _state

    private val callbacks = Callbacks(initialCallbacks)

    override suspend fun registerCallback(callback: FormStateCallback<V>) =
        callbacks.register(callback)

    override suspend fun unregisterCallback(callback: FormStateCallback<V>) =
        callbacks.unregister(callback)

    internal suspend fun updateState(transform: (state: FormState<V>) -> FormState<V>): FormState<V> =
        stateMutex.withLock {
            _state = transform(_state)
            broadcastState(state)
            _state
        }

    internal suspend fun broadcastState(state: FormState<V>) {
        callbacks.broadcast(state)
    }
}
