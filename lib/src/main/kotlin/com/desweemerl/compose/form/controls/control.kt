package com.desweemerl.compose.form.controls

import com.desweemerl.compose.form.Callback
import com.desweemerl.compose.form.CallbacksImpl
import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.validators.Validators
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface Control<S> {
    val validators: Validators<S>
    val state: S

    suspend fun transform(transformer: Transformer<S>): S

    suspend fun registerCallback(callback: Callback<S>)
    suspend fun unregisterCallback(callback: Callback<S>)

    suspend fun validate(): S
}

abstract class AbstractControl<S>(
    initialState: S,
    initialCallbacks: List<Callback<S>>? = null,
) : Control<S> {
    private val stateMutex = Mutex()
    internal var internalState = initialState

    override val state: S
        get() = internalState

    private val callbacks = CallbacksImpl(initialCallbacks)

    override suspend fun registerCallback(callback: Callback<S>) =
        callbacks.register(callback)

    override suspend fun unregisterCallback(callback: Callback<S>) =
        callbacks.unregister(callback)

    internal suspend inline fun updateState(transformer: Transformer<S>): S =
        stateMutex.withLock {
            internalState = transformer(internalState)
            broadcastState()
            internalState
        }

    internal suspend inline fun broadcastState() {
        callbacks.broadcast(state)
    }
}

abstract class AbstractFormControl<S, V>(initialState: S) :
    AbstractControl<S>(initialState = initialState) where S : FormState<V> {

    @Suppress("UNCHECKED_CAST")
    suspend fun setValue(transformer: Transformer<V>): FormState<V> =
        transform { state -> state.setValue(transformer) as S }

    @Suppress("UNCHECKED_CAST")
    suspend fun markAsTouched(touched: Boolean = true): FormState<V> =
        transform { state -> state.markAsTouched(touched) as S }

    @Suppress("UNCHECKED_CAST")
    suspend fun markAsDirty(dirty: Boolean = true): FormState<V> =
        transform { state -> state.markAsDirty(dirty) as S }
}
