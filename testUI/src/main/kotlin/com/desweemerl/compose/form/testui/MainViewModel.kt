package com.desweemerl.compose.form.testui

import androidx.lifecycle.ViewModel
import com.desweemerl.compose.form.*
import com.desweemerl.compose.form.validators.FormValidator
import com.desweemerl.compose.form.validators.ValidatorPattern
import com.desweemerl.compose.form.validators.ValidatorRequired


val matchPasswordValidation = object : FormValidator<Map<String, Any>>(message = "") {
    override suspend fun validate(state: IFormState<Map<String, Any>>): ValidationErrors? {
        val password = state.value.get("password") as? String ?: ""
        val confirmPassword = state.value.get("confirmation_password") as? String ?: ""
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