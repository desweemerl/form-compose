package com.desweemerl.compose.form.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.desweemerl.compose.form.Path
import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.controls.*
import kotlinx.coroutines.launch

@Composable
fun FormTextField(
    state: FormFieldState<String>,
    onStateChanged: (FormFieldState<String>) -> Unit = {},
    label: String? = null,
    error: @Composable (FormFieldState<String>) -> Unit = { FormFieldError(it) },
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

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 5.dp)
    ) {
        OutlinedTextField(
            label = label?.let { { Text(text = label) } },
            singleLine = singleLine,
            value = state.value,
            isError = state.errors.isNotEmpty(),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            onValueChange = { fieldValue ->
                onStateChanged(state.copy(value = fieldValue, dirty = true))
            },
            modifier = Modifier
                .testTag(testTag)
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focused && focusState.isFocused) {
                        focused = true
                    } else if (focused && !focusState.isFocused && !state.touched) {
                        onStateChanged(state.copy(touched = true))
                    }
                }
        )

        error(state)
    }
}

val errorsWhenTouched: Transformer<FormFieldState<String>> =
    { state -> if (state.touched) state else state.copy(errors = listOf()) }

@Suppress("UNCHECKED_CAST")
@Composable
fun Control<*>?.asTextField(
    label: String? = null,
    singleLine: Boolean = true,
    password: Boolean = false,
    transformer: Transformer<FormFieldState<String>> = errorsWhenTouched,
    error: @Composable (FormState<String>) -> Unit = { FormFieldError(it) },
    onEnter: (() -> Unit)? = null,
    testTag: String = "",
) = (this as? FormFieldControl<String>)?.let {
    var state by asMutableState()
    val scope = rememberCoroutineScope()

    FormTextField(
        state = transformer(state),
        onStateChanged = { newState ->
            state = newState
            scope.launch {
                transform { newState }
            }
        },
        label = label,
        error = error,
        singleLine = singleLine,
        password = password,
        onEnter = onEnter,
        testTag = testTag,
    )
}

@Preview
@Composable
fun PreviewFormTextField() {
    textControl("My value").asTextField()
}

@Preview
@Composable
fun PreviewFormPasswordField() {
    val state = FormFieldState("Secret")

    FormTextField(state = state, password = true)
}

@Preview
@Composable
fun PreviewFormTextFieldWithError() {
    val state = FormFieldState(
        "My value",
        touched = true,
        errors = listOf(ValidationError("preview", "preview error", Path()))
    )

    FormTextField(state = state)
}