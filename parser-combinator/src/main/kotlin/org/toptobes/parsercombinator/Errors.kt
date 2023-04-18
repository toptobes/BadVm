package org.toptobes.parsercombinator

interface ErrorResult {
    fun rootCause(pretty: Boolean = true): ErrorResult? {
        return if (this is NestedError) cause.rootCause(pretty) else this
    }

    val prettyErrorMsg get() = toString()
}

interface NestedError : ErrorResult {
    val cause: ErrorResult
}

data class BasicErrorResult(val desc: String) : ErrorResult {
    override fun toString() = desc
}

interface DescriptiveErrorResult : ErrorResult {
    val parserName: String
    val targetIndex: Int
}

data class EndOfInputError(
    override val parserName: String,
    override val targetIndex: Int,
) : DescriptiveErrorResult {
    override val prettyErrorMsg: String get() {
        return "$parserName: Unexpected end of input @ target index $targetIndex"
    }
}

data class MatchError(
    override val parserName: String,
    override val targetIndex: Int,
    val pattern: Any?
) : DescriptiveErrorResult {
    override val prettyErrorMsg: String get() {
        return "$parserName: Could not match '$pattern' @ target index $targetIndex"
    }
}

data class UnexpectedMatchError(
    override val parserName: String,
    override val targetIndex: Int,
    val expected: Any?,
    val got: Any?,
) : DescriptiveErrorResult {
    override val prettyErrorMsg: String get() {
        return "$parserName: expected $expected, got $got @ target index $targetIndex"
    }
}

data class SequenceError(
    override val parserName: String,
    override val targetIndex: Int,
    val parserIndex: Int,
    override val cause: ErrorResult,
) : DescriptiveErrorResult, NestedError {
    override val prettyErrorMsg: String get() {
        return "$parserName, parser #$parserIndex: $cause"
    }
}

data class NoMatchError(
    override val parserName: String,
    override val targetIndex: Int,
) : DescriptiveErrorResult {
    override val prettyErrorMsg: String get() {
        return "$parserName: No matches @ target index $targetIndex"
    }
}

data class NoNonErrorsError(
    override val parserName: String,
    override val targetIndex: Int,
    val errors: List<ErrorResult?>
) : DescriptiveErrorResult {
    override val prettyErrorMsg: String get() {
        return "$parserName: No matches; errors = $errors"
    }

    override fun rootCause(pretty: Boolean): ErrorResult {
        return ErrorListWrapper(pretty, errors.map { it?.rootCause() })
    }

    class ErrorListWrapper(val pretty: Boolean, val errors: List<ErrorResult?>) : ErrorResult {
        override fun toString() = if (pretty) "${errors.map { "\"${it?.prettyErrorMsg}\"" }}" else "$errors"
    }
}

data class TrailingSepError(
    override val parserName: String,
    override val targetIndex: Int,
    val separator: String? = null,
) : DescriptiveErrorResult {
    override val prettyErrorMsg: String get() {
        return "$parserName: Disallowed trailing separator ${separator?.let { "($it)" } ?: ""} @ target index $targetIndex"
    }
}
