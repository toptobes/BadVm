package org.toptobes.lang.parsers

import org.toptobes.lang.nodes.NodeToDelete
import org.toptobes.parsercombinator.impls.*

private const val SINGLE_LINE_COMMENT_START = "#"

private const val MULTI_LINE_COMMENT_START = "#="
private const val MULTI_LINE_COMMENT_END   = "=#"

fun commentParser() = any(multilineComment, singleLineComment)

private val singleLineComment = sequence(
    str(SINGLE_LINE_COMMENT_START),
    until(nextLetter) { it.result == "\n" }
).map { NodeToDelete }

private val multilineComment = sequence(
    str(MULTI_LINE_COMMENT_START),
    until(nextLetter, checkAtEndOfLoop = true) { it.target[it.index - 2].toString() + it.result == MULTI_LINE_COMMENT_END }
).map { NodeToDelete }
