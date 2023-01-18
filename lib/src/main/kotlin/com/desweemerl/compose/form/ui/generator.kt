package com.desweemerl.compose.form.ui

import androidx.compose.runtime.Composable
import com.desweemerl.compose.form.FormState

typealias Generator<V> = @Composable (state: FormState<V>) -> (@Composable () -> Unit)?