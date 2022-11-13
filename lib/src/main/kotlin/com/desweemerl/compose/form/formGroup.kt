package com.desweemerl.compose.form

import com.desweemerl.compose.form.validators.FormValidator
import com.desweemerl.compose.form.validators.FormValidators
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FormGroupState(
    val controlValues: Map<String, Any>,
    val generateErrors: () -> ValidationErrors,

    val formValue: Map<String, Any> = mapOf(),
    val formErrors: ValidationErrors = listOf(),

    internal var controlErrorsDirty: Boolean = true,

    override val dirty: Boolean = false,
    override val touched: Boolean = false,
    override val validating: Boolean = false,
    override val validationRequested: Boolean = false,
) : IFormState<Map<String, Any>> {
    private var _errors: ValidationErrors? = null

    override val value: Map<String, Any>
        get() = controlValues.plus(formValue)

    fun withControlValues(newControlValues: Map<String, Any>): FormGroupState = FormGroupState(
        controlValues = newControlValues,
        generateErrors = generateErrors,
        formValue = formValue,
        formErrors = formErrors,
        controlErrorsDirty = controlErrorsDirty,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    override fun withValue(newValue: Map<String, Any>): FormGroupState = FormGroupState(
        controlValues = controlValues,
        generateErrors = generateErrors,
        formValue = newValue,
        formErrors = formErrors,
        controlErrorsDirty = controlErrorsDirty,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    override val errors: ValidationErrors
        get() {
            if (_errors == null || controlErrorsDirty) {
                _errors = generateErrors()
                controlErrorsDirty = false
            }

            return _errors!!.plus(formErrors)
        }

    override fun withErrors(newErrors: ValidationErrors): FormGroupState = FormGroupState(
        controlValues = controlValues,
        generateErrors = generateErrors,
        formValue = formValue,
        formErrors = newErrors,
        controlErrorsDirty = controlErrorsDirty,
        dirty = dirty,
        touched = touched,
        validating = validating,
        validationRequested = validationRequested,
    )

    override fun markAsValidating(newValidating: Boolean): FormGroupState =
        FormGroupState(
            controlValues = controlValues,
            generateErrors = generateErrors,
            formValue = formValue,
            formErrors = formErrors,
            controlErrorsDirty = controlErrorsDirty,
            dirty = dirty,
            touched = touched,
            validating = newValidating,
            validationRequested = validationRequested,
        )

    override fun requestValidation(requested: Boolean): FormGroupState =
        FormGroupState(
            controlValues = controlValues,
            generateErrors = generateErrors,
            formValue = formValue,
            formErrors = formErrors,
            controlErrorsDirty = controlErrorsDirty,
            dirty = dirty,
            touched = touched,
            validating = validating,
            validationRequested = requested,
        )

    override fun markAsTouched(newTouched: Boolean): FormGroupState =
        FormGroupState(
            controlValues = controlValues,
            generateErrors = generateErrors,
            formValue = formValue,
            formErrors = formErrors,
            controlErrorsDirty = controlErrorsDirty,
            dirty = dirty,
            touched = newTouched,
            validating = validating,
            validationRequested = validationRequested,
        )

    override fun markAsDirty(newDirty: Boolean): FormGroupState =
        FormGroupState(
            controlValues = controlValues,
            generateErrors = generateErrors,
            formValue = formValue,
            formErrors = formErrors,
            controlErrorsDirty = controlErrorsDirty,
            dirty = newDirty,
            touched = touched,
            validating = validating,
            validationRequested = validationRequested,
        )

    fun markControlErrorsDirty(): FormGroupState =
        FormGroupState(
            controlValues = controlValues,
            generateErrors = generateErrors,
            formValue = formValue,
            formErrors = formErrors,
            controlErrorsDirty = true,
            dirty = dirty,
            touched = touched,
            validating = validating,
            validationRequested = validationRequested,
        )
}


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
) : AbstractFormControl<Map<String, Any>>(
    FormGroupState(
        controlValues = controls.getValues(),
        generateErrors = { controls.getErrors() },
    )
),
    IFormGroupControl {
    private var transformJob: Job? = null
    private var validationJob: Job? = null

    init {
        scope.launch {
            controls.entries.map { entry ->
                entry.value.registerCallback { controlState ->
                    try {
                        scope.launch {
                            updateState { formState ->
                                (formState as FormGroupState)
                                    .withControlValues(
                                        formState.controlValues.plus(Pair(entry.key, controlState.value))
                                    )
                                    .markControlErrorsDirty()
                            }

                            if (!controlState.validating && !internalState.validating) {
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

    override suspend fun transformValue(transform: (value: Map<String, Any>) -> Map<String, Any>): IFormState<Map<String, Any>> {
        transformJob?.cancel()
        transformJob = scope.launch {
            val newValue = transform(state.value)
            val controlEntries = newValue.entries.filter { entry -> controls.containsKey(entry.key) }
            val formValue = newValue.filterKeys { key -> !controls.containsKey(key) }

            updateState { formState ->
                (formState as FormGroupState)
                    .withValue(formValue)
                    .withControlValues(
                        newValue.filterKeys { key -> controls.containsKey(key) }
                    )
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

    override suspend fun validate(): IFormState<Map<String, Any>> = liveValidate(true)

    private suspend fun liveValidate(validationRequested: Boolean = false): IFormState<Map<String, Any>> {
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
                    (formState as FormGroupState)
                        .withErrors(errors)
                        .markControlErrorsDirty()
                        .markAsValidating(false)
                        .requestValidation(false)
                }
            } catch (ex: Exception) {
                updateState { formState ->
                    (formState as FormGroupState)
                        .markControlErrorsDirty()
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