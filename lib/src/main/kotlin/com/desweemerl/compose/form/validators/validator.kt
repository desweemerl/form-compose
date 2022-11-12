package com.desweemerl.compose.form.validators

import com.desweemerl.compose.form.ValidationErrors

interface IValidator<S> {
    suspend fun validate(state: S): ValidationErrors?
}