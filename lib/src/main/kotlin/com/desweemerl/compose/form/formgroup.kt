package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidator
import com.desweemerl.compose.form.validators.FormValidators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

typealias FormGroupState = IFormState<Map<String, Any>>

fun Map<String, IFormControl<Any>>.getValues(): Map<String, Any> =
    entries.associate { entry -> Pair(entry.key, entry.value.state.value) }


class FormGroupControl(
    val controls: Map<String, IFormControl<Any>> = mapOf(),
    override val validators: FormValidators<Map<String, Any>> = arrayOf(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : AbstractFormControl<Map<String, Any>>(FormState(value = controls.getValues())) {
    private var syncJob: Job
    private var transformJob: Job? = null
    private var validationJob: Job? = null

    init {
        syncJob = scope.launch {
            controls.entries.forEach { entry ->
                entry.value.registerCallback { state ->
                    mergeControlState(entry.key, state)
                }
            }
        }
    }

    fun getControl(key: String): IFormControl<Any>? = controls[key]

    private suspend fun mergeControlState(
        key: String,
        controlState: IFormState<Any>
    ): FormGroupState = updateState { state ->
        val newValue = state.value.plus(Pair(key, controlState.value))
        val newErrors = state.errors.replace(key, controlState.errors)

        state.withValue(newValue).withErrors(newErrors)
    }

    override suspend fun transformValue(transform: (value: Map<String, Any>) -> Map<String, Any>): FormGroupState {
        transformJob?.cancel()
        transformJob = scope.launch {
            val newValue = transform(state.value)
            val entries = newValue.entries
                .filter { entry -> controls.containsKey(entry.key) }

            entries.map { entry ->
                launch {
                    controls[entry.key]?.transformValue { entry.value }
                }
            }.joinAll()

            updateState { state ->
                val patchValue = state.value.plus(
                    newValue.filterKeys { key -> !controls.containsKey(key) }
                )

                state.withValue(patchValue)
            }
        }
        transformJob?.join()

        return state
    }

    override suspend fun validate(): FormGroupState {
        validationJob?.cancel()
        validationJob = scope.launch {
            val initialState = updateState { state ->
                state
                    .clearErrors()
                    .markAsValidating()
                    .requestValidation()
            }

            try {
                val mutex = Mutex()
                var errors = listOf<ValidationError>()

                listOf(
                    scope.launch {
                        validators.map { validator ->
                            launch {
                                validator.validate(initialState)?.let { error ->
                                    mutex.withLock {
                                        errors = errors + error
                                    }
                                }
                            }
                        }.joinAll()
                    },

                    scope.launch {
                        controls.entries.map { entry ->
                            launch {
                                val controlErrors = entry.value.validate().errors
                                mutex.withLock {
                                    errors = errors.replace(entry.key, controlErrors)
                                }
                            }
                        }.joinAll()

                    }).joinAll()

                updateState { state ->
                    state
                        .withErrors(errors)
                        .markAsValidating(false)
                        .requestValidation(false)
                }
            } catch (ex: Exception) {
                updateState { state ->
                    state
                        .markAsValidating(false)
                        .requestValidation()
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

    fun <V> withControl(key: String, state: IFormControl<V>): FormGroupBuilder {
        @Suppress("UNCHECKED_CAST")
        controls[key] = state as IFormControl<Any>
        return this
    }

    fun withValidator(validator: FormValidator<Map<String, Any>>): FormGroupBuilder {
        validators.add(validator)
        return this
    }

    fun build(): FormGroupControl = FormGroupControl(controls = controls)
}