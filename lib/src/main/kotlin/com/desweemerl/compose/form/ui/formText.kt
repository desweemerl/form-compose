package com.desweemerl.compose.form.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.desweemerl.compose.form.controls.FormState

enum class MessageType {
    HINT, ERROR
}

@Composable
fun FormText(
    state: FormState<*>,
    modifier: Modifier = Modifier,
    hint: String? = null,
    visibleLines: Int = 1,
) {
    if (visibleLines > 0) {
        if (state.errors.isEmpty()) {
            FormText(
                hint ?: "",
                modifier = modifier,
                messageType = MessageType.HINT,
                visibleLines = visibleLines,

            )
        } else {
            val errorMessage = state.errors.first().message
            FormText(
                errorMessage,
                modifier = modifier,
                messageType = MessageType.ERROR,
                visibleLines = visibleLines,
            )
        }
    }
}

@Composable
fun FormText(
    message: String,
    modifier: Modifier = Modifier,
    messageType: MessageType = MessageType.HINT,
    visibleLines: Int = 1,
    typography: TextStyle = MaterialTheme.typography.bodySmall,
) {
    val lineHeight = typography.lineHeight * visibleLines
    val lineHeightDp: Dp = with(LocalDensity.current) {
        lineHeight.toDp()
    }

    val color = when (messageType) {
        MessageType.ERROR -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.fillMaxWidth().height(lineHeightDp)
    )
}