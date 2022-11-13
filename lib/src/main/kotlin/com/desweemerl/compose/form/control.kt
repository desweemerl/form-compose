package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidators
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


typealias FormStateCallback<V> = Callback<IFormState<V>>

interface IFormControl<V> {
    val validators: FormValidators<V>
    val state: IFormState<V>

    suspend fun registerCallback(callback: FormStateCallback<V>)
    suspend fun unregisterCallback(callback: FormStateCallback<V>)

    suspend fun transformValue(transform: (value: V) -> V): IFormState<V>
    suspend fun validate(): IFormState<V>
}

abstract class AbstractFormControl<V>(
    initialState: IFormState<V>,
    initialCallbacks: List<Callback<IFormState<V>>>? = null,
) : IFormControl<V> {
    private val stateMutex = Mutex()
    internal var internalState = initialState

    override val state: IFormState<V>
        get() = internalState

    private val callbacks = Callbacks(initialCallbacks)

    override suspend fun registerCallback(callback: FormStateCallback<V>) =
        callbacks.register(callback)

    override suspend fun unregisterCallback(callback: FormStateCallback<V>) =
        callbacks.unregister(callback)

    internal suspend fun updateState(broadCast: Boolean = true, transform: (state: IFormState<V>) -> IFormState<V>): IFormState<V> =
        stateMutex.withLock {
            internalState = transform(internalState)
            if (broadCast) { broadcastState(state) }
            internalState
        }

    internal suspend fun broadcastState(state: IFormState<V>) {
        callbacks.broadcast(state)
    }
}
