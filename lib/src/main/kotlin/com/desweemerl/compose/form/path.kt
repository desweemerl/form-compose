package com.desweemerl.compose.form


class Path(vararg varParts: String) {
    private var _parts: Array<String>
    val parts: Array<String>
        get() = _parts

    private val pathRE = "^/?((?:[^/]+/)*(?:[^/]+))$".toRegex()

    init {
        _parts = if (varParts.size == 1) {
            val value = varParts[0]
            val groups = pathRE.find(value)
                ?: throw ValidationException("path $value is incorrect")

            groups.groupValues[1].split("/").toTypedArray()
        } else {
            varParts.forEach { part ->
                if (part.isEmpty()) {
                    throw ValidationException("part $part must not be empty")
                }

                if (part.contains("/")) {
                    throw ValidationException("part $part must not contain char '/'")
                }
            }

            arrayOf(*varParts)
        }
    }

    override fun toString(): String =
        "/${_parts.joinToString("/")}"

    fun plus(path: Path): Path =
        Path(*_parts.plus(path._parts))

    fun isEmpty(): Boolean = _parts.isEmpty()

    fun includePath(path: Path): Boolean =
        path.parts.withIndex().all { iValue ->
            parts.size > iValue.index && iValue.value == parts[iValue.index]
        }

    override fun equals(other: Any?): Boolean =
        if (other is Path) {
            other.toString() == toString()
        } else {
            false
        }

    override fun hashCode(): Int = parts.hashCode()
}
