package com.desweemerl.compose.form.ui


import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.desweemerl.compose.form.FormState

@Composable
fun <V> FormFieldError(state: FormState<V>) {
    if (state.errors.isNotEmpty()) {
        val firstError = state.errors.first()

        Text(
            text = firstError.message,
            style = MaterialTheme.typography.caption,
            color = Color.Red,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
