package com.desweemerl.compose.form


class ValidationException(override val message: String) : Exception(message)

typealias ValidationErrors = List<ValidationError>

class ValidationError(
    val type: String,
    val message: String,
    val path: Path = Path(),
) {
    override fun toString(): String =
        "Validation error(type='$type' message='$message' path='$path')"

    override fun equals(other: Any?): Boolean =
        if (other is ValidationError) {
            type == other.type && path == other.path
        } else {
            false
        }

    override fun hashCode(): Int = toString().hashCode()
}

fun ValidationErrors.prependPath(path: String): ValidationErrors =
    prependPath(Path(path))

fun ValidationErrors.prependPath(path: Path): ValidationErrors =
    map{ error -> ValidationError(type = error.type, message = error.message, path = path.plus(error.path))}

fun ValidationErrors.replace(path: String, errors: ValidationErrors): ValidationErrors =
    replace(Path(path), errors)

fun ValidationErrors.replace(path: Path, errors: ValidationErrors): ValidationErrors =
    filter { error ->
        (path.isEmpty() && !error.path.isEmpty())
                || (!path.isEmpty() && !error.path.includePath(path))
    }
        .plus(errors.map { error ->
            ValidationError(
                type = error.type,
                message = error.message,
                path = path.plus(error.path),
            )
        })

fun ValidationErrors.hasErrorOfType(type: String): Boolean = any { error -> error.type == type }

fun ValidationErrors.hasErrorOfType(type: String, path: Path): Boolean = any { error ->
    error.type == type && error.path.includePath(path)
}

fun ValidationErrors.withPath(path: Path): ValidationErrors =
    filter { error -> error.path == path }

fun ValidationErrors.withEmptyPath(): ValidationErrors =
    filter { error -> error.path.isEmpty() }

fun ValidationErrors.first(): ValidationError? = null

fun ValidationErrors.matches(errors: ValidationErrors?): Boolean =
    errors != null
            && size == errors.size
            && all { e -> errors.any { error -> e.equals(error) } }