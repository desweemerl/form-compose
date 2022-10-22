package com.desweemerl.compose.form.ui

import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.tooling.preview.Preview
import com.desweemerl.compose.form.FormControl
import com.desweemerl.compose.form.FormState
import com.desweemerl.compose.form.IFormState
import kotlinx.coroutines.launch

@Composable
fun FormTextField(
    flowState: FormControl<String>
) {
    val state = flowState.state.collectAsState()

    FormTextField(
        state = state.value,
        onStateChanged = flowState::update,
    )
}

@Composable
fun FormTextField(
    state: IFormState<String>,
    onStateChanged: (IFormState<String>) -> Unit = {},
)  {
    val coroutineScope = rememberCoroutineScope()

    TextField(
        value = state.value,
        onValueChange = { fieldValue ->
            coroutineScope.launch {
                onStateChanged(
                    state
                        .withValue(fieldValue)
                        .clearErrors()
                        .markAsTouched()
                        .markAsDirty()
                )
            }
        },
        modifier = Modifier.onFocusChanged {
            coroutineScope.launch {
                onStateChanged(state.markAsTouched())
            }
        }
    )
}

@Preview
@Composable
fun PreviewFormFieldState() {
    val state = FormState("My value")

    FormTextField(state = state)
}