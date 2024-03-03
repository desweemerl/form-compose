package com.desweemerl.compose.form.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.desweemerl.compose.form.Path
import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.controls.*
import kotlinx.coroutines.launch

enum class FormTextFieldStyle {
    NORMAL,
    OUTLINED,
}


@Composable
fun FormTextField(
    state: FormFieldState<String>,
    onStateChanged: (FormFieldState<String>) -> Unit = {},
    style: FormTextFieldStyle = FormTextFieldStyle.NORMAL,
    keyboardType: KeyboardType? = null,
    label: String? = null,
    placeholder: String? = null,
    hint: String? = null,
    singleLine: Boolean = true,
    password: Boolean = false,
    visibleBottomLines: Int = 1,
    onEnter: (() -> Unit)? = null,
    testTag: String = "",
) {
    var keyboardOptions = keyboardType?.let { KeyboardOptions(keyboardType = it )}
        ?: KeyboardOptions.Default

    var visualTransformation = VisualTransformation.None
    val keyboardActions = if (onEnter == null) {
        KeyboardActions.Default
    } else {
        KeyboardActions(onDone = { onEnter() })
    }

    var focused by remember { mutableStateOf(false) }

    if (password) {
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType ?: KeyboardType.Password)
        visualTransformation = PasswordVisualTransformation()
    }

    if (style == FormTextFieldStyle.OUTLINED) {
        OutlinedTextField(
            value = state.value,
            onValueChange = { newValue: String ->
                onStateChanged(state.copy(value = newValue, dirty = true))
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
                },
            enabled = state.enabled,
            label = label?.let { { Text(text = it) } },
            placeholder = placeholder?.let { { Text(text = it) } },
            supportingText = {
                FormText(
                    state = state,
                    hint = hint,
                    visibleLines = visibleBottomLines
                )
            },
            isError = state.errors.isNotEmpty(),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
        )
    } else {
        TextField(
            value = state.value,
            onValueChange = { newValue: String ->
                onStateChanged(state.copy(value = newValue, dirty = true))
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
                },
            enabled = state.enabled,
            label = label?.let { { Text(text = it) } },
            placeholder = placeholder?.let { { Text(text = it) } },
            supportingText = {
                FormText(
                    state = state,
                    hint = hint,
                    visibleLines = visibleBottomLines
                )
            },
            isError = state.errors.isNotEmpty(),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
        )
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
fun <V> Control<V>?.asTextField(
    style: FormTextFieldStyle = FormTextFieldStyle.NORMAL,
    transformer: Transformer<FormFieldState<V>> = ::errorsWhenTouched,
    label: String? = null,
    placeholder: String? = null,
    hint: String? = null,
    singleLine: Boolean = true,
    visibleBottomLines: Int = 1,
    password: Boolean = false,
    onEnter: (() -> Unit)? = null,
    testTag: String = "",
) = (this as? FormFieldControlConverters<V, String>)?.let {
    val state by stateFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val keyboardType = when (state.value) {
        is Int? -> if (password) KeyboardType.NumberPassword else KeyboardType.Number
        else -> null
    }

    FormTextField(
        state = to(transformer(state)),
        onStateChanged = { newState ->
            scope.launch {
                transform { from(newState) }
            }
        },
        style = style,
        keyboardType = keyboardType,
        label = label,
        placeholder = placeholder,
        hint = hint,
        singleLine = singleLine,
        visibleBottomLines = visibleBottomLines,
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
fun PreviewOutlinedFormTextField() {
    textControl("My value").asTextField(style = FormTextFieldStyle.OUTLINED)
}

@Preview
@Composable
fun PreviewLabelFormTextField() {
    textControl("My value").asTextField(label = "My label")
}

@Preview
@Composable
fun PreviewLabelOutlinedFormTextField() {
    textControl("My value").asTextField(
        label = "My label",
        style = FormTextFieldStyle.OUTLINED,
    )
}

@Preview
@Composable
fun PreviewPlaceHolderFormTextField() {
    textControl("").asTextField(placeholder = "My placeholder")
}

@Preview
@Composable
fun PreviewPlaceHolderOutlinedFormTextField() {
    textControl("").asTextField(
        style = FormTextFieldStyle.OUTLINED,
        placeholder = "My placeholder",
    )
}

@Preview
@Composable
fun PreviewPasswordWithHintFormTextField() {
    textControl("Secret").asTextField(
        password = true,
        hint = "Please provide\na password",
        visibleBottomLines = 2,
    )
}

@Preview
@Composable
fun PreviewPasswordWithHintOutlinedFormTextField() {
    textControl("Secret").asTextField(
        style = FormTextFieldStyle.OUTLINED,
        password = true,
        hint = "Please provide\na password",
        visibleBottomLines = 2,
    )
}

@Preview
@Composable
fun PreviewErrorFormTextField() {
    val state = FormFieldState(
        "My value",
        touched = true,
        errors = listOf(ValidationError("preview", "Preview error", Path()))
    )

    FormTextField(state = state, hint = "This hint should be hidden")
}

@Preview
@Composable
fun PreviewErrorOutlinedFormTextField() {
    val state = FormFieldState(
       "My value",
        touched = true,
        errors = listOf(ValidationError("preview", "Preview error", Path())),
    )

    FormTextField(
        state = state,
        style = FormTextFieldStyle.OUTLINED,
        hint = "This hint should be hidden",
    )
}