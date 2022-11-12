package com.desweemerl.compose.form

fun <V> assertMatch(expected: V, actual: V) {
    if (expected != actual) {
        throw AssertionError("errors don't match:\nexpected=$expected\nactual=$actual")
    }
}

fun assertMatchErrors(expected: ValidationErrors, actual: ValidationErrors?) {
    if (!expected.matches(actual)) {
        throw AssertionError("errors don't match:\nexpected=$expected\nactual=$actual")
    }
}