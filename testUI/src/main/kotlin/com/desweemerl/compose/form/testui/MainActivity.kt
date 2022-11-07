package com.desweemerl.compose.form.testui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desweemerl.compose.form.textControl
import com.desweemerl.compose.form.ui.FormTextField
import com.desweemerl.compose.form.validators.ValidatorPattern
import com.desweemerl.compose.form.validators.ValidatorRequired
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val control = textControl("initial",arrayOf(
            ValidatorRequired("Value required"),
            ValidatorPattern("^test$", "Wrong value"),
        ))

        setContent {
            val scope = rememberCoroutineScope()

            Column {
                FormTextField(control)

                Spacer(modifier = Modifier.height(10.dp))

                Button(onClick = { scope.launch { control.transformValue { "initial" } } }) {
                    Text("Reset")
                }
            }
        }
    }
}