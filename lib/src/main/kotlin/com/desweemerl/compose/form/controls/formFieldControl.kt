package com.desweemerl.compose.form.controls

import com.desweemerl.compose.form.Converters
import com.desweemerl.compose.form.FormFieldStateIntConverters
import com.desweemerl.compose.form.FormFieldStateStringConverters
import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.validators.Validators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class FormFieldControl<V>(
    initialState: FormFieldState<V>,
    override val validators: Validators<FormFieldState<V>>,
    private val scope: CoroutineScope,
    private var parentControl: FormGroupControl? = null,
) : AbstractFormControl<FormFieldState<V>, V>(initialState) {
    private var validationJob: Job? = null

    private suspend fun updateAndNotify(transformer: (state: FormFieldState<V>) -> FormFieldState<V>): FormFieldState<V> {
        val newState = updateState(transformer)
        parentControl?.notifyStateChange()
        return newState
    }

    override suspend fun transform(transformer: (state: FormFieldState<V>) -> FormFieldState<V>): FormFieldState<V> {
        updateAndNotify(transformer = transformer)
        liveValidate()

        return state
    }

    override suspend fun validate(): FormFieldState<V> =
        liveValidate(true)

    override fun bind(control: Control<*>) {
        if (parentControl == null) {
            if (control is FormGroupControl) {
                parentControl = control
            } else {
                throw Exception("control must be a FormGroupControl")
            }
        } else {
            throw Exception("control is already bound")
        }
    }

    private suspend fun liveValidate(validationRequested: Boolean = false): FormFieldState<V> {
        validationJob?.cancelAndJoin()

        if (!state.enabled) {
            validationJob = null
            return updateAndNotify { state ->
                state.copy(errors = listOf())
            }
        }

        validationJob = scope.launch {
            val initialState = updateAndNotify { state ->
                state.copy(
                    validating = true,
                    validationRequested = validationRequested
                )
            }

            val errors = mutableListOf<ValidationError>()

            try {
                val mutex = Mutex()
                // Execute all validations at same time
                validators.map { validator ->
                    scope.launch {
                        validator.validate(initialState)?.let { validationErrors ->
                            mutex.withLock {
                                errors.addAll(validationErrors)
                            }
                        }
                    }
                }.joinAll()

                updateAndNotify { state ->
                    state.copy(
                        validating = false,
                        validationRequested = false,
                        errors = errors
                    )
                }
            } catch (ex: Exception) {
                updateAndNotify { state ->
                    state.copy(
                        validating = false,
                        validationRequested = false,
                    )
                }
                throw ex
            }
        }
        validationJob?.join()

        return state
    }
}

class FormFieldControlConverters<V, O>(
    initialState: FormFieldState<V>,
    private val converters: Converters<FormFieldState<V>, FormFieldState<O>>,
    override val validators: Validators<FormFieldState<V>>,
    scope: CoroutineScope,
    parentControl: FormGroupControl? = null,
) : FormFieldControl<V>(
    initialState = initialState,
    validators = validators,
    scope = scope,
    parentControl = parentControl
),
    Converters<FormFieldState<V>, FormFieldState<O>> by converters

fun textControl(
    initialValue: String = "",
    validators: Validators<FormFieldState<String>> = arrayOf(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): FormFieldControlConverters<String, String> =
    FormFieldControlConverters(
        FormFieldState(initialValue),
        FormFieldStateStringConverters,
        validators,
        scope
    )

fun intControl(
    initialValue: Int? = null,
    validators: Validators<FormFieldState<Int?>> = arrayOf(),
    allowNegative: Boolean = true,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): FormFieldControlConverters<Int?, String> =
    FormFieldControlConverters(
        FormFieldState(initialValue),
        FormFieldStateIntConverters(allowNegative = allowNegative),
        validators,
        scope
    )

fun boolControl(
    initialValue: Boolean = false,
    validators: Validators<FormFieldState<Boolean>> = arrayOf(),
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
): FormFieldControl<Boolean> =
    FormFieldControl(FormFieldState(initialValue), validators, scope)

