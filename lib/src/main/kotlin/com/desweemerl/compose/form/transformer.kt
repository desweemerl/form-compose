package com.desweemerl.compose.form

class Transformer<V> {
    private var errorFn: ((state: IFormState<V>) -> ValidationErrors)? = null

    fun errors(fn: (state: IFormState<V>) -> ValidationErrors): Transformer<V> {
        errorFn = fn
        return this
    }

    fun transform(state: IFormState<V>): IFormState<V> =
        errorFn
            ?.let { fn -> state.withErrors(fn(state))  }
            ?: state

    companion object {
        fun <V> errors(fn: (state: IFormState<V>) -> ValidationErrors) = Transformer<V>().errors(fn)

        fun <V> default() = Transformer<V>().errors{ state -> if (state.touched) state.errors else listOf() }
    }
}