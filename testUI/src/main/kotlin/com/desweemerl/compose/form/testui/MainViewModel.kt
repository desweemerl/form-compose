package com.desweemerl.compose.form.testui

import androidx.lifecycle.ViewModel
import com.desweemerl.compose.form.*
import com.desweemerl.compose.form.controls.*
import com.desweemerl.compose.form.validators.*


val matchPasswordValidation = object : AbstractValidator<FormGroupState>(message = "") {
    override suspend fun validate(state: FormGroupState): ValidationErrors? {
        val password = state.value["password"] as? String ?: ""
        val confirmPassword = state.value["confirmation_password"] as? String ?: ""
        return if (password.isNotEmpty() && password != confirmPassword) {
            listOf(
                ValidationError(
                    "password",
                    "passwords mismatch",
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
                    ValidatorPattern("^[0-9a-zA-Z]{8,20}$", "invalid password"),
                )
            )
        )
        .withControl(
            "confirmation_password",
            textControl(validators = arrayOf(ValidatorRequired()))
        )
        .withControl(
            "age",
            intControl(18, validators = arrayOf(
                ValidatorMin(12),
                ValidatorMax(99),
            ))
        )
        .withControl(
            "keep_registered",
            boolControl(true)
        )
        .withValidator(matchPasswordValidation)
        .build()
}