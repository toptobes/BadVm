package org.toptobes.lang.parsing

import org.toptobes.lang.ast.DeleteThisNode
import org.toptobes.parsercombinator.ifOkay
import org.toptobes.parsercombinator.impls.*

private const val SINGLE_LINE_COMMENT_START = "#"

private const val MULTI_LINE_COMMENT_START = "#="
private const val MULTI_LINE_COMMENT_END   = "=#"

val commentParser = lazy { any(multilineComment, singleLineComment) }

private val singleLineComment = sequence(
    str(SINGLE_LINE_COMMENT_START),
    until(nextLetter) { it.ifOkay { result == "\n" } ?: false }
).map { DeleteThisNode }

private val multilineComment = sequence(
    str(MULTI_LINE_COMMENT_START),
    until(nextLetter, checkAtEndOfLoop = true) { it.ifOkay { target[index - 2].toString() + result == MULTI_LINE_COMMENT_END  } ?: false  }
).map { DeleteThisNode }
