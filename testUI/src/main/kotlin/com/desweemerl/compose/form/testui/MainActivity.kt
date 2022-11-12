package com.desweemerl.compose.form.testui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.desweemerl.compose.form.Transformer
import com.desweemerl.compose.form.ui.asMutableState
import com.desweemerl.compose.form.ui.asTextField
import com.desweemerl.compose.form.withEmptyPath
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel by viewModels()
            val scope = rememberCoroutineScope()
            val form = viewModel.form

            val formState by form.asMutableState()

            Column {
                form.getControl("password").asTextField(password = true)

                form.getControl("confirmation_password").asTextField(
                    password = true,
                    transformer = Transformer.errors { state ->
                        if (state.touched) {
                            state.errors + formState.errors.withEmptyPath()
                        } else {
                            listOf()
                        }
                    }
                )

                Button(onClick = { scope.launch { form.validate() } }) {
                    Text("Submit")
                }
            }
        }
    }
}