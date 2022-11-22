package com.desweemerl.compose.form.controls

import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.validators.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

interface Control<S> {
    val validators: Validators<S>
    val state: S
    val stateFlow: StateFlow<S>

    fun bind(control: Control<*>)

    suspend fun transform(transformer: Transformer<S>): S
    suspend fun validate(): S
}

abstract class AbstractControl<S>(initialState: S) : Control<S> {
    private val _state = MutableStateFlow(initialState)

    override val state: S
        get() = _state.value

    override val stateFlow: StateFlow<S>
        get() = _state.asStateFlow()

    internal open fun updateState(transformer: Transformer<S>): S =
        _state.getAndUpdate(transformer)
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
