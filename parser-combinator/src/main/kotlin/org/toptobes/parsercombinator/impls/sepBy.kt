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

    lateinit var lastOkayState: ParseState<*>

    while (true) {
        if (nextSeparatorState != oldState) {
            prevNextContentState = nextContentState
        }

        nextContentState = content.parsePropagating(nextSeparatorState)

        if (nextContentState.isErrored) {
            lastOkayState = nextSeparatorState

            if (!allowTrailingSep && nextSeparatorState != oldState) {
                return@Parser errored(prevNextContentState, TrailingSepError("sepBy", prevNextContentState.index, null))
            }
            break
        } else {
            results += nextContentState.result!!
        }

        nextSeparatorState = separator.parsePropagating(nextContentState)

        if (nextSeparatorState.isErrored) {
            lastOkayState = nextContentState
            break
        }
    }

    if (requireMatch && results.isEmpty()) {
        return@Parser errored(nextSeparatorState, NoMatchError("sepBy", nextSeparatorState.index))
    }

    return@Parser succeed(lastOkayState, results)
}

fun <NewT> commas(content: Parser<NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
    sepBy(content, -str(","), allowTrailingSep, requireMatch)

fun <NewT> periods(content: Parser<NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
    sepBy(content, -str("."), allowTrailingSep, requireMatch)

fun <NewT> whitespace(content: Parser<NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
    sepBy(content, whitespace, allowTrailingSep, requireMatch)

fun <NewT> optionalWhitespace(
    content: Parser<NewT>,
    allowTrailingSep: Boolean = true,
    requireMatch: Boolean = false
) = sepBy(content, whitespace withDefault "", allowTrailingSep, requireMatch)
