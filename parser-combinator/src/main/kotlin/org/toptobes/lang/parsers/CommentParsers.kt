package org.toptobes.lang.parsers

import org.toptobes.lang.DeleteThisNode
import org.toptobes.parsercombinator.impls.*

const val SINGLE_LINE_COMMENT_START = "#"

const val MULTI_LINE_COMMENT_START = "#="
const val MULTI_LINE_COMMENT_END   = "=#"

fun commentParser() = any(multilineComment, singleLineComment)

val singleLineComment = sequence(
    str(SINGLE_LINE_COMMENT_START),
    until(nextLetter) { it.result == "\n" }
).map { DeleteThisNode }

val multilineComment = sequence(
    str(MULTI_LINE_COMMENT_START),
    until(nextLetter, checkAtEndOfLoop = true) { it.target[it.index - 2].toString() + it.result == MULTI_LINE_COMMENT_END }
).map { DeleteThisNode }
