package com.desweemerl.compose.form

import org.junit.ComparisonFailure

fun <V> assertMatch(expected: V, actual: V) {
    if (expected != actual) {
        throw ComparisonFailure("", expected.toString(), actual.toString())
    }
}

fun assertMatchErrors(expected: ValidationErrors, actual: ValidationErrors?) {
    if (!expected.matches(actual)) {
        throw AssertionError("errors don't match:\nexpected=$expected\nactual=$actual")
    }
}