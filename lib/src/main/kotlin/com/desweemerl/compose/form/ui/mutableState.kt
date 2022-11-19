package com.desweemerl.compose.form.ui

import androidx.compose.runtime.*
import com.desweemerl.compose.form.Callback
import com.desweemerl.compose.form.controls.Control
import com.desweemerl.compose.form.controls.FormState
import kotlinx.coroutines.launch

@Composable
fun <S, V> Control<S>.asMutableState(): MutableState<S> where S : FormState<V> {
    val state = remember() { mutableStateOf(this.state) }
    val scope = rememberCoroutineScope()

    DisposableEffect(key1 = Unit) {
        val callback: Callback<S> = { newState ->
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