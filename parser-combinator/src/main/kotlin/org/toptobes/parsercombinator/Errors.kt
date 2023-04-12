package org.toptobes.parsercombinator

interface ErrorResult

interface DescriptiveErrorResult : ErrorResult {
    val parserName: String
    val targetIndex: Int
}

data class EndOfInputError(
    override val parserName: String,
    override val targetIndex: Int,
) : DescriptiveErrorResult {
    val errorMsg: String get() {
        return "EndOfInputError(\"$parserName: Unexpected end of input @ target index $targetIndex\")"
    }
}

data class MatchError(
    override val parserName: String,
    override val targetIndex: Int,
    val pattern: Any?
) : DescriptiveErrorResult {
    val errorMsg: String get() {
        return "MatchError(\"$parserName: Could not match '$pattern' @ target index $targetIndex\")"
    }
}

data class UnexpectedMatchError(
    override val parserName: String,
    override val targetIndex: Int,
    val expected: Any?,
    val got: Any?,
) : DescriptiveErrorResult {
    val errorMsg: String get() {
        return "MatchError(\"$parserName: expected $expected, got $got @ target index $targetIndex\")"
    }
}

data class SequenceError(
    override val parserName: String,
    override val targetIndex: Int,
    val parserIndex: Int,
    val error: ErrorResult,
) : DescriptiveErrorResult {
    val errorMsg: String get() {
        return "SequenceError(\"$parserName, parser #$parserIndex: $error\")"
    }
}

data class NoMatchError(
    override val parserName: String,
    override val targetIndex: Int,
) : DescriptiveErrorResult {
    val errorMsg: String get() {
        return "NoMatchError(\"$parserName: No matches @ target index $targetIndex\")"
    }
}

data class TrailingSepError(
    override val parserName: String,
    override val targetIndex: Int,
    val seperator: String? = null,
) : DescriptiveErrorResult {
    val errorMsg: String get() {
        return "TrailingCommaError(\"$parserName: Disallowed trailing seperator ${seperator?.let { "($it)" } ?: ""} @ target index $targetIndex\")"
    }
}
