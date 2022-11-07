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
import kotlinx.coroutines.launch

@Composable
fun FormTextField(
    formControl: FormControl<String>,
    label: String? = null,
    singleLine: Boolean = true,
    password: Boolean = false,
    formError: Generator<String> = @Composable { FormFieldError(it) },
    onEnter: (() -> Unit)? = null,
    testTag: String = "",
) {
    var state by remember {
        mutableStateOf(formControl.state)
    }

    val scope = rememberCoroutineScope()

    DisposableEffect(key1 = Unit) {
        val callback: FormStateCallback<String> = { newState ->
            if (!state.matches(newState)) {
                state = newState
            }
        }

        scope.launch { formControl.registerCallback(callback) }

        onDispose {
            scope.launch { formControl.unregisterCallback(callback) }
        }
    }

    FormTextField(
        state = state,
        onStateChanged = { newState ->
            state = newState
            formControl.update(newState)
        },
        label = label,
        formError = formError,
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
    formError: Generator<String> = @Composable { FormFieldError(it) },
    singleLine: Boolean = true,
    password: Boolean = false,
    onEnter: (() -> Unit)? = null,
    testTag: String = "",
) {
    val coroutineScope = rememberCoroutineScope()

    var keyboardOptions = KeyboardOptions.Default
    var visualTransformation = VisualTransformation.None
    val keyboardActions = if (onEnter == null) {
        KeyboardActions.Default
    } else {
        KeyboardActions(onDone = { onEnter() })
    }

    val error = formError(state)

    if (password) {
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        visualTransformation = PasswordVisualTransformation()
    }

    Column {
         OutlinedTextField(
            label = label?.let { { Text(text = label) } },
            singleLine = singleLine,
            value = state.value,
            isError = error != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            onValueChange = { fieldValue ->
                coroutineScope.launch {
                    onStateChanged(
                        state
                            .withValue(fieldValue)
                            .markAsTouched()
                            .markAsDirty()
                    )
                }
            },
            modifier = Modifier
                .testTag(testTag)
                .onFocusChanged {
                    coroutineScope.launch {
                        onStateChanged(state.markAsTouched())
                    }
                }
        )

        if (error != null) {
            error()
        }
    }
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