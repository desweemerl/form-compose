package com.desweemerl.compose.form

import kotlinx.coroutines.flow.*

interface IFlowState<S> {
    val state: Flow<S>
}

interface IValue<V> {
    suspend fun transformValue(transform: (value: V) -> V)
}
