package org.toptobes.lang.utils

import org.toptobes.parsercombinator.ParseState

class ParsingException(msg: String): Exception(msg)

class DescriptiveParsingException(msg: String, state: ParseState<String, *>) : Exception("""
    |> ${state.getErrorLine()} <|
    ${state.getErrorIndexArrow()}
    ERROR: $msg
""".trimIndent())

private fun ParseState<String, *>.getErrorLine(): String {
    val lineStart = target.substring(0, index).substringAfterLast("\r").substringAfterLast("\n")
    val lineEnd = target.substring(index).substringBefore("\r").substringBefore("\n")
    return lineStart + lineEnd
}

private fun ParseState<String, *>.getErrorIndexArrow(): String {
    val spacesLen = target.substring(0, (index + 1).coerceAtMost(target.length)).substringAfterLast("\r").substringAfterLast("\n").length
    val caretsLen = target.substring(index).substringBefore(" ").length
    return "   " + " ".repeat((spacesLen - 1).coerceAtLeast(0)) + "^".repeat(caretsLen + 1)
}
