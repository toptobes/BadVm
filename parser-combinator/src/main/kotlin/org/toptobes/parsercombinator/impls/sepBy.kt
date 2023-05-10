@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

// sigh
fun <R> sepBy(
    content: Parser<R>,
    separator: Parser<*>,
    allowTrailingSep: Boolean = true,
    requireMatch: Boolean = false
) = Parser { oldState ->
    val results = mutableListOf<R>()

    var nextSeparatorState: ParseState<*> = oldState
    lateinit var prevNextContentState: ParseState<*>
    lateinit var nextContentState: ParseState<*>

    lateinit var lastOkayState: OkayParseState<*>

    while (true) {
        if (nextSeparatorState != oldState) {
            prevNextContentState = nextContentState
        }

        nextContentState = content.parsePropagating(nextSeparatorState)

        if (nextContentState.isErrored()) {
            lastOkayState = nextSeparatorState as OkayParseState

            if (!allowTrailingSep && nextSeparatorState != oldState) {
                return@Parser errored(prevNextContentState, "sepBy: Illegal trailing comma")
            }
            break
        } else {
            results += nextContentState.result
        }

        nextSeparatorState = separator.parsePropagating(nextContentState)

        if (nextSeparatorState.isErrored()) {
            lastOkayState = nextContentState
            break
        }
    }

    if (requireMatch && results.isEmpty()) {
        return@Parser errored(nextSeparatorState, "sepBy: No matches found")
    }

    return@Parser success(lastOkayState, results)
}

fun <NewT> sepByCommas(content: Parser<NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
    sepBy(content, -str(","), allowTrailingSep, requireMatch)

fun <NewT> sepByPeriods(content: Parser<NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
    sepBy(content, -str("."), allowTrailingSep, requireMatch)

fun <NewT> sepByWhitespace(content: Parser<NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
    sepBy(content, whitespace, allowTrailingSep, requireMatch)

fun <NewT> sepByOptionalWhitespace(
    content: Parser<NewT>,
    allowTrailingSep: Boolean = true,
    requireMatch: Boolean = false
) = sepBy(content, whitespace withDefault "", allowTrailingSep, requireMatch)
