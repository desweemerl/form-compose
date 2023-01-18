package com.desweemerl.compose.form.testui

import androidx.lifecycle.ViewModel
import com.desweemerl.compose.form.*
import com.desweemerl.compose.form.controls.FormGroupBuilder
import com.desweemerl.compose.form.controls.FormGroupState
import com.desweemerl.compose.form.controls.textControl
import com.desweemerl.compose.form.validators.AbstractValidator
import com.desweemerl.compose.form.validators.ValidatorPattern
import com.desweemerl.compose.form.validators.ValidatorRequired


val matchPasswordValidation = object : AbstractValidator<FormGroupState>(message = "") {
    override suspend fun validate(state: FormGroupState): ValidationErrors? {
        val password = state.value["password"] as? String ?: ""
        val confirmPassword = state.value["confirmation_password"] as? String ?: ""
        return if (password.isNotEmpty() && password != confirmPassword) {
            listOf(
                ValidationError(
                    "password",
                    "Passwords mismatch",
                )
            )
        } else {
            null
        }
    }
}

class MainViewModel : ViewModel() {
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
}