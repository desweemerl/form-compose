package com.desweemerl.compose.form.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.desweemerl.compose.form.*

@Composable
fun FormTextField(
    control: FormControl<String>,
    label: String? = null,
    singleLine: Boolean = true,
    password: Boolean = false,
    transformer: Transformer<String> = Transformer.default(),
    error: @Composable (FormState<String>) -> Unit = { FormFieldError(it) },
    onEnter: (() -> Unit)? = null,
    testTag: String = "",
) {
    val state = control.asMutableState()

    FormTextField(
        state = transformer.transform(state.value),
        onStateChanged = { newState ->
            state.value = newState
            control.update(newState)
        },
        label = label,
        error = error,
        singleLine = singleLine,
        password = password,
        onEnter = onEnter,
        testTag = testTag,
    )
}

@Composable
fun FormTextField(
    state: FormState<String>,
    onStateChanged: (FormState<String>) -> Unit = {},
    label: String? = null,
    error: @Composable (FormState<String>) -> Unit = { FormFieldError(it) },
    singleLine: Boolean = true,
    password: Boolean = false,
    onEnter: (() -> Unit)? = null,
    testTag: String = "",
) {
    var keyboardOptions = KeyboardOptions.Default
    var visualTransformation = VisualTransformation.None
    val keyboardActions = if (onEnter == null) {
        KeyboardActions.Default
    } else {
        KeyboardActions(onDone = { onEnter() })
    }

    var focused by remember { mutableStateOf(false) }

    if (password) {
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        visualTransformation = PasswordVisualTransformation()
    }

    Column {
        OutlinedTextField(
            label = label?.let { { Text(text = label) } },
            singleLine = singleLine,
            value = state.value,
            isError = state.errors.isNotEmpty(),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            onValueChange = { fieldValue ->
                onStateChanged(
                    state
                        .withValue(fieldValue)
                        .markAsDirty()
                )
            },
            modifier = Modifier
                .testTag(testTag)
                .onFocusChanged { focusState ->
                    if (!focused && focusState.isFocused) {
                        focused = true
                    } else if (focused && !focusState.isFocused && !state.touched) {
                        onStateChanged(state.markAsTouched())
                    }
                }
        )

        error(state)
    }
}

@Composable
fun IFormControl<Any>?.asTextField(
    label: String? = null,
    singleLine: Boolean = true,
    password: Boolean = false,
    transformer: Transformer<String> = Transformer.default(),
    error: @Composable (FormState<String>) -> Unit = { FormFieldError(it) },
    onEnter: (() -> Unit)? = null,
    testTag: String = "",
) = (this as? FormControl<String>)?.let {
    FormTextField(
        this,
        label = label,
        singleLine = singleLine,
        password = password,
        transformer = transformer,
        error = error,
        onEnter = onEnter,
        testTag = testTag
    )
}

@Preview
@Composable
fun PreviewFormTextField() {
    val control = textControl("My value")

    FormTextField(control)
}

@Preview
@Composable
fun PreviewFormPasswordField() {
    val state = FormState("Secret")

    FormTextField(state = state, password = true)
}

@Preview
@Composable
fun PreviewFormTextFieldWithError() {
    val state = FormState(
        "My value",
        touched = true,
        errors = listOf(ValidationError("preview", "preview error", Path()))
    )

    FormTextField(state = state)
}