package com.desweemerl.compose.form


class ValidationException(override val message: String) : Exception(message)

interface IValidationError {
    val type: String
    val message: String
    val path: IPath
}

typealias Errors = List<IValidationError>

class ValidationError(
    override val type: String,
    override val message: String,
    override val path: IPath = Path(),
) : IValidationError {
    override fun toString(): String =
        "Validation error: type=${type} message=${message} path=${path}"

    override fun equals(other: Any?): Boolean {
        if (other is ValidationError) {
            return other.type == type && other.message == message && other.path == path
        }
        return false
    }

    override fun hashCode(): Int = toString().hashCode()
}

fun Errors.merge(path: String, errors: Errors): Errors = merge(Path(path), errors)
fun Errors.merge(path: IPath, errors: Errors): Errors =
    filter { error -> !error.path.includePath(path) }
        .plus(errors.map { error ->
            ValidationError(
                type = error.type,
                message = error.message,
                path = path.plus(error.path),
            )
        })


