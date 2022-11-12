package com.desweemerl.compose.form.testui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.desweemerl.compose.form.*
import com.desweemerl.compose.form.ui.*
import com.desweemerl.compose.form.validators.FormValidator
import com.desweemerl.compose.form.validators.ValidatorPattern
import com.desweemerl.compose.form.validators.ValidatorRequired
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val matchPasswordValidation = object : FormValidator<Map<String, Any>>(message = "") {
            override suspend fun validate(state: FormState<Map<String, Any>>): ValidationError? {
                val password = state.value.get("password") as? String ?: ""
                val confirmPassword = state.value.get("confirmation_password") as? String ?: ""
                return if (password.isNotEmpty() && password != confirmPassword) {
                    ValidationError(
                        "password",
                        "Passwords mismatch",
                    )
                } else {
                    null
                }
            }
        }
        setContent {
            val scope = rememberCoroutineScope()

            val form = FormGroupBuilder()
                .withControl(
                    "password",
                    textControl(
                        validators = arrayOf(
                            ValidatorRequired(),
                            ValidatorPattern("^[0-9a-zA-Z]{8,20}$", "Invalid password"),
                        )
                    )
                )
                .withControl(
                    "confirmation_password",
                    textControl(validators = arrayOf(ValidatorRequired()))
                )
                .withValidator(matchPasswordValidation)
                .build()

            val formState = form.asMutableState()

            Column {
                form.getControl("password").asTextField(password = true)

                form.getControl("confirmation_password").asTextField(
                    password = true,
                    transformer = Transformer.errors { state ->
                        if (state.touched) {
                            state.errors + formState.value.errors.withEmptyPath()
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