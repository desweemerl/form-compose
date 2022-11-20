package com.desweemerl.compose.form.testui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desweemerl.compose.form.controls.mergeErrors
import com.desweemerl.compose.form.pipe
import com.desweemerl.compose.form.ui.asMutableState
import com.desweemerl.compose.form.ui.asTextField
import com.desweemerl.compose.form.ui.errorsWhenTouched
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel by viewModels()
            val scope = rememberCoroutineScope()
            val form = viewModel.form

            val formState by form.asMutableState()

            Column(modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 10.dp, horizontal = 5.dp)
            ) {
                form.getControl("password").asTextField(password = true)

                form.getControl("confirmation_password").asTextField(
                    password = true,
                    transformer = pipe(mergeErrors { formState }, errorsWhenTouched),
                )

                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Button(onClick = {
                        scope.launch {
                            form.markAsTouched()
                            form.validate()
                        }
                    }) {
                        Text("Submit")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(onClick = {
                        scope.launch {
                            form.markAsTouched(false)
                        }
                    }) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}