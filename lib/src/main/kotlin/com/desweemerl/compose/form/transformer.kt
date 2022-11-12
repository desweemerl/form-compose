package com.desweemerl.compose.form

class Transformer<V> {
    private var errorFn: ((state: FormState<V>) -> ValidationErrors)? = null

    fun errors(fn: (state: FormState<V>) -> ValidationErrors): Transformer<V> {
        errorFn = fn
        return this
    }

    fun transform(state: FormState<V>): FormState<V> =
        errorFn
            ?.let { fn -> state.withErrors(fn(state))  }
            ?: state

    companion object {
        fun <V> errors(fn: (state: FormState<V>) -> ValidationErrors) = Transformer<V>().errors(fn)

        fun <V> default() = Transformer<V>().errors{ state -> if (state.touched) state.errors else listOf() }
    }
}