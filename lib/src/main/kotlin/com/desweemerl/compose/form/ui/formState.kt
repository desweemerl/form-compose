package com.desweemerl.compose.form.ui

import androidx.compose.runtime.*
import com.desweemerl.compose.form.FormState
import com.desweemerl.compose.form.FormStateCallback
import com.desweemerl.compose.form.IFormControl
import kotlinx.coroutines.launch

@Composable
fun <V> IFormControl<V>.asMutableState(): MutableState<FormState<V>> {
    val state = remember() { mutableStateOf(this.state) }
    val scope = rememberCoroutineScope()

    DisposableEffect(key1 = Unit) {
        val callback: FormStateCallback<V> = { newState ->
            if (!state.value.matches(newState)) {
                state.value = newState
            }
        }

        scope.launch { this@asMutableState.registerCallback(callback) }

        onDispose {
            scope.launch { this@asMutableState.unregisterCallback(callback) }
        }
    }

    return state
}
