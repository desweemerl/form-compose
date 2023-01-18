package com.desweemerl.compose.form

interface IPath {
    val parts: Array<String>

    fun includePath(path: IPath): Boolean =
        path.parts.withIndex().all { iValue ->
            iValue.value == parts[iValue.index]
        }

    fun plus(path: IPath): IPath

    fun isEmpty(): Boolean = parts.isEmpty()
}

class Path(vararg varParts: String) : IPath {
    override lateinit var parts: Array<String>

    private val pathRE = "^/?((?:[^/]+/)*(?:[^/]+))$".toRegex()

    init {
        parts = if (varParts.size == 1) {
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
        "/${parts.joinToString("/")}"

    override fun plus(path: IPath): IPath =
        Path(*parts.plus(path.parts))

    override fun equals(other: Any?): Boolean {
        if (other is Path) {
            return other.toString() == toString()
        }

        return false
    }

    override fun hashCode(): Int = parts.hashCode()
}
