package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidator
import com.desweemerl.compose.form.validators.FormValidatorOptions
import com.desweemerl.compose.form.validators.FormValidators
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias FormGroupState = IFormState<Map<String, Any>>

private sealed class FormGroupChange {
    class FieldChange(val field: String, val controlState: IFormState<Any>) : FormGroupChange()
    class FormChange(val state: FormGroupState) : FormGroupChange()
}

class FormGroupControl(
    val controls: Map<String, IFormControl<Any>> = mapOf(),
    override val validators: FormValidators<Map<String, Any>> = arrayOf(),
) : IFormControl<Map<String, Any>> {
    private val controlFlows
        get() = controls.entries.map { entry ->
            entry.value.state.map { state -> FormGroupChange.FieldChange(entry.key, state) }
        }.toTypedArray()

    private val controlStates
        get() = merge(*controlFlows)

    private val formStateChange =
        MutableSharedFlow<FormGroupChange.FormChange>()

    fun getControl(key: String): IFormControl<Any>? = controls[key]

    private val mutex = Mutex()
    private var formState: FormGroupState = FormState(value = mapOf())

    override val state: Flow<FormGroupState> =
        merge(formStateChange, controlStates).map { change ->
            mutex.withLock {
                formState = when (change) {
                    is FormGroupChange.FieldChange -> {
                        val newValue =
                            formState.value.plus(Pair(change.field, change.controlState.value))
                        val errors =
                            formState.errors.merge(change.field, change.controlState.errors)

                        formState.withValue(newValue).withErrors(errors)
                    }
                    is FormGroupChange.FormChange -> {
                        val newValue = formState.value.plus(change.state.value)
                        formState.withValue(newValue).withErrors(change.state.errors)
                    }
                }
            }
            formState
        }

    override suspend fun transformValue(transform: (value: Map<String, Any>) -> Map<String, Any>) {
        mutex.withLock {
            val newValue = transform(formState.value)
            newValue.entries
                .forEach { entry ->
                    controls[entry.key]?.transformValue { entry.value }
                }

            val patchValue = newValue.filterKeys { key -> !controls.containsKey(key) }
            val newState = formState
                .withValue(patchValue)
                .clearErrors()

            formStateChange.emit(FormGroupChange.FormChange(newState))
        }
    }

    override suspend fun validate(): Errors {
        mutex.withLock {
            val initialState = formState.markAsValidating().clearErrors()
            formStateChange.emit(FormGroupChange.FormChange(initialState))
        }

        controls.values.forEach { control -> control.validate() }

        return mutex.withLock {
            val newState =
                validators.validate(formState, FormValidatorOptions()).markAsValidating(false)
            formStateChange.emit(FormGroupChange.FormChange(newState))

            newState.errors
        }
    }

    override suspend fun clearErrors() {
        controls.values.forEach{ control -> control.clearErrors() }
        mutex.withLock {
            val newState = formState.clearErrors()
            formStateChange.emit(FormGroupChange.FormChange(newState))
       }
    }
}

class FormGroupBuilder {
    private var controls = mutableMapOf<String, IFormControl<Any>>()
    private val validators = mutableListOf<FormValidator<Map<String, Any>>>()

    fun <V> withControl(key: String, state: IFormControl<V>): FormGroupBuilder {
        controls[key] = state as IFormControl<Any>
        return this
    }

    fun withValidator(validator: FormValidator<Map<String, Any>>): FormGroupBuilder {
        validators.add(validator)
        return this
    }

    fun build(): FormGroupControl = FormGroupControl(controls = controls)
}