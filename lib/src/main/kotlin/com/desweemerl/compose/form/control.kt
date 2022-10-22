package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidatorOptions
import com.desweemerl.compose.form.validators.FormValidators
import com.desweemerl.compose.form.validators.IFormValidatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

interface IFormControl<V> : IFlowState<IFormState<V>>, IValue<V> {
    val validators: FormValidators<V>
    suspend fun clearErrors()
    suspend fun validate(): Errors
}

class FormControl<V>(
    initialState: IFormState<V>,
    override val validators: FormValidators<V>,
) :  IFormControl<V> {
    private val _state = MutableStateFlow(initialState)
    override val state = _state.asStateFlow()

    override suspend fun transformValue(transform: (value: V) -> V) {
        _state.getAndUpdate { state -> state.withValue(transform(state.value)) }
        validateWithOptions(FormValidatorOptions(force = false))
    }

    private suspend fun validateWithOptions(options: IFormValidatorOptions): Errors {
        val initialState = _state.value.markAsValidating().clearErrors()
        _state.update { initialState }

        val nextState =  validators.validate(initialState, options).markAsValidating(false)
        _state.update { nextState }

        return nextState.errors
    }

    override suspend fun validate(): Errors = validateWithOptions(FormValidatorOptions())

    override suspend fun clearErrors() {
        _state.getAndUpdate { state -> state.clearErrors() }
    }

    fun update(newState: IFormState<V>) {
        _state.update { newState }
    }
}

fun textControl(initialValue: String = "", validators: FormValidators<String> = arrayOf())
    : FormControl<String> =
        FormControl(FormState(initialValue), validators)
