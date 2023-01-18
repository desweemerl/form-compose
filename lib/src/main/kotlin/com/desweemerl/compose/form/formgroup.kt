package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidator
import com.desweemerl.compose.form.validators.FormValidators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias FormGroupState = FormState<Map<String, Any>>

fun Map<String, IFormControl<Any>>.getValues(): Map<String, Any> =
    entries.associate { entry -> Pair(entry.key, entry.value.state.value) }

fun Map<String, IFormControl<Any>>.getErrors(): ValidationErrors =
    entries
        .filter { entry -> entry.value.state.errors.isNotEmpty() }
        .map { entry -> entry.value.state.errors.prependPath(entry.key) }.flatten()

interface IFormGroupControl : IFormControl<Map<String, Any>> {
    fun getControl(key: String): IFormControl<Any>?
}

internal class FormGroupControl(
    val controls: Map<String, IFormControl<Any>> = mapOf(),
    override val validators: FormValidators<Map<String, Any>> = arrayOf(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : AbstractFormControl<Map<String, Any>>(FormState(value = mapOf())),
    IFormGroupControl {
    private var transformJob: Job? = null
    private var validationJob: Job? = null

    private val controlMutex = Mutex()
    private var controlValues = controls.getValues().toMutableMap()

    init {
        scope.launch {
            controls.entries.map { entry ->
                entry.value.registerCallback { controlState ->
                    try {
                        scope.launch {
                            controlMutex.withLock {
                                controlValues[entry.key] = controlState.value
                            }

                            if (!controlState.validating && !_state.validating) {
                                liveValidate()
                            } else {
                                broadcastState(state)
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    override fun getControl(key: String): IFormControl<Any>? = controls[key]

    override val state
        get() = FormState(
            value = _state.value + controlValues,
            errors = _state.errors + controls.getErrors(),
        )

    override suspend fun transformValue(transform: (value: Map<String, Any>) -> Map<String, Any>): FormGroupState {
        transformJob?.cancel()
        transformJob = scope.launch {
            val newValue = transform(state.value)
            val controlEntries = newValue.entries.filter { entry -> controls.containsKey(entry.key) }
            val formValue = newValue.filterKeys { key -> !controls.containsKey(key) }

            controlMutex.withLock {
                controlValues = newValue.filterKeys { key -> controls.containsKey(key) }.toMutableMap()
            }

            updateState { formState ->
                formState.withValue(formValue)
            }

            controlEntries.map { entry ->
                launch {
                    controls[entry.key]?.transformValue { entry.value }
                }
            }.joinAll()

            liveValidate()
        }
        transformJob?.join()

        return state
    }

    override suspend fun validate(): FormGroupState = liveValidate(true)

    private suspend fun liveValidate(validationRequested: Boolean = false): FormGroupState {
        validationJob?.cancel()
        validationJob = scope.launch {
            updateState { formState ->
                formState
                    .markAsValidating()
                    .requestValidation(validationRequested)
            }

            val initialState = state

            try {
                val mutex = Mutex()
                val errors = mutableListOf<ValidationError>()

                val jobs = mutableListOf(
                    launch {
                        validators.map { validator ->
                            launch {
                                validator.validate(initialState)?.let { validationErrors ->
                                    mutex.withLock {
                                        errors.addAll(validationErrors)
                                    }
                                }
                            }
                        }.joinAll()
                    }
                )

                if (validationRequested) {
                    jobs.add(
                        launch {
                            controls.entries
                                .map { entry -> launch { entry.value.validate() } }
                                .joinAll()
                        }
                    )
                }

                jobs.joinAll()

                updateState { formState ->
                    formState
                        .withErrors(errors)
                        .markAsValidating(false)
                        .requestValidation(false)
                }
            } catch (ex: Exception) {
                updateState { formState ->
                    formState
                        .markAsValidating(false)
                        .requestValidation(false)
                }
                throw ex
            }
        }
        validationJob?.join()

        return state
    }
}

class FormGroupBuilder {
    private var controls = mutableMapOf<String, IFormControl<Any>>()
    private val validators = mutableListOf<FormValidator<Map<String, Any>>>()

    fun withControl(key: String, state: IFormControl<*>): FormGroupBuilder {
        @Suppress("UNCHECKED_CAST")
        controls[key] = state as IFormControl<Any>
        return this
    }

    fun withValidator(validator: FormValidator<Map<String, Any>>): FormGroupBuilder {
        validators.add(validator)
        return this
    }

    fun build(): IFormGroupControl = FormGroupControl(
        controls = controls,
        validators = validators.toTypedArray(),
    )
}