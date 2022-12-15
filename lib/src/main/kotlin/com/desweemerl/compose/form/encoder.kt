package com.desweemerl.compose.form

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

typealias JsonElementEncoder = (value: Any?) -> JsonElement

fun valueJsonEncoder(value: Any?): JsonElement =
    when (value) {
        is Map<*, *> -> {
            val mapJsonElements = value
                .map { entry ->
                    val entryKey =
                        entry.key as? String ?: throw Exception("map key must be a string (got ${entry.key})")
                    val entryValue = valueJsonEncoder(entry.value)
                    Pair(entryKey, entryValue)
                }
                .toMap()

            JsonObject(mapJsonElements)
        }
        is String -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Int -> JsonPrimitive(value)
        null -> JsonNull
        else -> throw Exception("type ${value::class} not supported")
    }