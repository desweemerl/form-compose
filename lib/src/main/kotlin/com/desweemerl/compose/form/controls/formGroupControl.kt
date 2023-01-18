package com.desweemerl.compose.form.controls

import com.desweemerl.compose.form.*
import com.desweemerl.compose.form.validators.Validator
import com.desweemerl.compose.form.validators.Validators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun Map<String, Control<FormState<Any>>>.getValues(): Map<String, Any> =
    entries.associate { entry -> Pair(entry.key, entry.value.state.value) }

fun Map<String, Control<FormState<Any>>>.getErrors(): ValidationErrors =
    entries
        .filter { entry -> entry.value.state.errors.isNotEmpty() }
        .map { entry -> entry.value.state.errors.prependPath(entry.key) }.flatten()

fun Map<String, Control<FormState<Any>>>.touched(): Boolean =
    values.any { control -> control.state.touched }

fun Map<String, Control<FormState<Any>>>.dirty(): Boolean =
    values.any { control -> control.state.dirty }

class FormGroupControl(
    private val controls: Map<String, AbstractFormControl<FormState<Any>, Any>> = mapOf(),
    override val validators: Validators<FormGroupState> = arrayOf(),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private var parentControl: FormGroupControl? = null,
) : AbstractFormControl<FormGroupState, Map<String, Any>>(FormGroupState(controls = controls)) {
    private var transformJob: Job? = null
    private var validationJob: Job? = null

    init {
        controls.values.forEach{ control -> control.bind(this) }
    }

    fun getControl(key: String): AbstractFormControl<FormState<Any>, Any>? = controls[key]

    private suspend fun updateAndNotify(transformer: (state: FormGroupState) -> FormGroupState): FormGroupState {
        val newState = updateState(transformer)
        parentControl?.notifyStateChange()

        return newState
    }

    internal suspend fun notifyStateChange() {
        if (transformJob?.isActive != true && validationJob?.isActive != true) {
            updateAndNotify { formState -> formState.copy(controlsDirty = true) }
            liveValidate()
        }
    }

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

    override suspend fun transform(transformer: (state: FormGroupState) -> FormGroupState): FormGroupState {
        transformJob?.cancelAndJoin()
        transformJob = scope.launch {
            val newState = transformer(state)

            newState.formValue
                .let { values ->
                    newState.controls.entries.associate { entry ->
                        Pair(
                            entry.key,
                            entry.value.state.value
                        )
                    }.plus(values)
                }
                .map { entry ->
                    launch {
                        controls[entry.key]?.transform(
                            pipe(
                                { controlState -> controlState.setValue { entry.value } },
                                filter(
                                    { newState.formTouched != null },
                                    { controlState -> controlState.markAsTouched(newState.formTouched!!) }),
                                filter(
                                    { newState.formDirty != null },
                                    { controlState -> controlState.markAsDirty(newState.formDirty!!) }),
                            )
                        )
                    }
                }
                .joinAll()

            updateAndNotify { newState.copy(formTouched = null, formDirty = null) }
            liveValidate()
        }
        transformJob?.join()

        return state
    }

    override suspend fun validate(): FormGroupState =
        liveValidate(true)

    private suspend fun liveValidate(validationRequested: Boolean = false): FormGroupState {
        validationJob?.cancelAndJoin()
        validationJob = scope.launch {
            updateAndNotify { state ->
                state.copy(validating = true, validationRequested = validationRequested)
            }

            try {
                val mutex = Mutex()
                val errors = mutableListOf<ValidationError>()
                val initialState = state

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

                updateAndNotify { state ->
                    state.copy(
                        formErrors = errors,
                        controlsDirty = true,
                        validating = false,
                        validationRequested = false,
                    )
                }
            } catch (ex: Exception) {
                updateAndNotify { state ->
                    state.copy(
                        controlsDirty = true,
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

class FormGroupBuilder {
    private var controls = mutableMapOf<String, AbstractFormControl<FormState<Any>, Any>>()
    private val validators = mutableListOf<Validator<FormGroupState>>()

    fun withControl(key: String, state: Control<*>): FormGroupBuilder {
        @Suppress("UNCHECKED_CAST")
        controls[key] = state as AbstractFormControl<FormState<Any>, Any>
        return this
    }

    fun withValidator(validator: Validator<FormGroupState>): FormGroupBuilder {
        validators.add(validator)
        return this
    }

    fun build(): FormGroupControl = FormGroupControl(
        controls = controls.toMap(),
        validators = validators.toTypedArray(),
    )
}