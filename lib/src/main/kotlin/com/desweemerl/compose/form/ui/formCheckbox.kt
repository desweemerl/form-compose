package com.desweemerl.compose.form.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desweemerl.compose.form.Path
import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.ValidationError
import com.desweemerl.compose.form.controls.*
import kotlinx.coroutines.launch

enum class FormCheckBoxAlign {
    LEFT,
    RIGHT,
}

@Composable
fun FormCheckbox(
    state: FormFieldState<Boolean>,
    onStateChanged: (FormFieldState<Boolean>) -> Unit = {},
    label: String? = null,
    align: FormCheckBoxAlign = FormCheckBoxAlign.RIGHT,
    hint: String? = null,
    visibleBottomLines: Int = 1,
    testTag: String = "",
) {
    var focused by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (label != null && align == FormCheckBoxAlign.LEFT) {
                Text(text = label, modifier = Modifier.padding(start = 15.dp))
            }

            Checkbox(
                checked = state.value,
                onCheckedChange = { fieldValue ->
                    onStateChanged(state.copy(value = fieldValue, dirty = true))
                },
                modifier = Modifier
                    .testTag(testTag)
                    .onFocusChanged { focusState ->
                        if (!focused && focusState.isFocused) {
                            focused = true
                        } else if (focused && !focusState.isFocused && !state.touched) {
                            onStateChanged(state.copy(touched = true))
                        }
                    }
            )

            if (label != null && align == FormCheckBoxAlign.RIGHT) {
                Text(text = label)
            }
        }

        FormText(
            state = state,
            hint = hint,
            visibleLines = visibleBottomLines,
            modifier = Modifier.padding(start = 15.dp),
        )
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
fun Control<*>?.asCheckbox(
    label: String? = null,
    align: FormCheckBoxAlign = FormCheckBoxAlign.RIGHT,
    hint: String? = null,
    visibleBottomLines: Int = 1,
    transformer: Transformer<FormFieldState<Boolean>> = ::errorsWhenTouched,
    testTag: String = "",
) = (this as? FormFieldControl<Boolean>)?.let {
    val state by stateFlow.collectAsState()
    val scope = rememberCoroutineScope()

    FormCheckbox(
        state = transformer(state),
        onStateChanged = { newState ->
            scope.launch {
                transform { newState }
            }
        },
        label = label,
        align = align,
        hint = hint,
        visibleBottomLines = visibleBottomLines,
        testTag = testTag,
    )
}

@Preview
@Composable
fun PreviewFormCheckbox() {
    boolControl(true).asCheckbox(visibleBottomLines = 0)
}

@Preview
@Composable
fun PreviewFormLabelCheckbox() {
    boolControl(true).asCheckbox(
        label = "My checkbox",
        hint = "Align right checkbox",
    )
}

@Preview
@Composable
fun PreviewFormLabelCheckboxAlignLeft() {
    boolControl(true).asCheckbox(
        label = "My checkbox",
        align = FormCheckBoxAlign.LEFT,
        hint = "Align left checkbox",
    )
}

@Preview
@Composable
fun PreviewFormCheckboxWithError() {
    val state = FormFieldState(
        false,
        touched = true,
        errors = listOf(ValidationError("preview", "preview error\ntest", Path()))
    )

    FormCheckbox(state = state, label = "My checkbox", visibleBottomLines = 2)
}