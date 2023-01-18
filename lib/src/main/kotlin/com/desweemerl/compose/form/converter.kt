package com.desweemerl.compose.form

import com.desweemerl.compose.form.controls.FormFieldState

interface Converters<V, O> {
    fun from(value: O): V
    fun to(value: V): O
}

object FormFieldStateStringConverters : Converters<FormFieldState<String>, FormFieldState<String>> {
    override fun from(value: FormFieldState<String>): FormFieldState<String> = value
    override fun to(value: FormFieldState<String>): FormFieldState<String> = value
}

val nonDecimalRE = Regex("[^\\d]")

fun String.processNumber(
    maxValue: String,
    allowNegative: Boolean = true
): String {
    val negativeValue = trim().startsWith("-")

    return run {
        if (negativeValue && !allowNegative) "" else replace(nonDecimalRE, "")
    }
        .let { value ->
            if (value.length >= maxValue.length) {
                val limitedValue = value.substring(0, maxValue.length)
                if (value.toCharArray().withIndex().any { char -> char.value.digitToInt() > maxValue[char.index].digitToInt() }) {
                    limitedValue.substring(0, limitedValue.length - 1)
                } else {
                    limitedValue
                }
            } else {
                value
            }
        }
        .let { value ->
            if (negativeValue) "-$value" else value
        }

}

fun stringToInt(value: String, allowNegative: Boolean = true): Int? = try {
    val processedNumber = value.processNumber(Int.MAX_VALUE.toString(), allowNegative)
    Integer.parseInt(processedNumber)
} catch (ex: NumberFormatException) {
    null
}

class FormFieldStateIntConverters(private val allowNegative: Boolean = true)
    : Converters<FormFieldState<Int?>, FormFieldState<String>> {

    override fun from(value: FormFieldState<String>): FormFieldState<Int?> =
        value.convert { stringToInt(it, allowNegative = allowNegative) }

    override fun to(value: FormFieldState<Int?>): FormFieldState<String> =
        value.convert { it?.toString() ?: "" }
}