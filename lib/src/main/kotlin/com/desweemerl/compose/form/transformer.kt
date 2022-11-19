package com.desweemerl.compose.form

typealias Transformer<V> = (value: V) -> V

fun <V> pipe(vararg transformers: Transformer<V>): Transformer<V> = { value ->
    transformers.fold(value) { v, f -> f(v) }
}

fun <V> filter(condition: (value: V) -> Boolean, vararg transformers: Transformer<V>)
    : Transformer<V> = { value ->
    if (condition(value)) {
        pipe(*transformers)(value)
    } else {
        value
    }
}
