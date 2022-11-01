package com.desweemerl.compose.form

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias Callback<V> = suspend (V) -> Unit

interface ICallbacks<V> {
    suspend fun register(callback: Callback<V>)
    suspend fun unregister(callback: Callback<V>)
    suspend fun broadcast(value: V)
}

class Callbacks<V> : ICallbacks<V> {
    private val callbacksMutex = Mutex()
    private val callbacks = mutableListOf<Callback<V>>()

    override suspend fun register(callback: Callback<V>): Unit =
        callbacksMutex.withLock {
            callbacks.add(callback)
        }

    override suspend fun unregister(callback: Callback<V>): Unit =
        callbacksMutex.withLock {
            callbacks.remove(callback)
        }

    override suspend fun broadcast(value: V) = callbacks.forEach { callback -> callback(value) }
}