@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

// sigh
class sepBy<T, R>(
    val content: Parser<T, R>,
    val separator: Parser<T, *>,
    val allowTrailingSep: Boolean = true,
    val requireMatch: Boolean = false
) : Parser<T, List<R>>() {
    override fun parse(oldState: ParseState<T, *>): ParseState<T, out List<R>> {
        val results = mutableListOf<R>()

        var nextSeparatorState: ParseState<T, *> = oldState
        lateinit var prevNextContentState: ParseState<T, *>
        lateinit var nextContentState: ParseState<T, *>

        lateinit var lastOkayState: ParseState<T, *>

        while (true) {
            if (nextSeparatorState != oldState) {
                prevNextContentState = nextContentState
            }

            nextContentState = content.parsePropagating(nextSeparatorState)

            if (nextContentState.isErrored) {
                lastOkayState = nextSeparatorState

                if (!allowTrailingSep && nextSeparatorState != oldState) {
                    return errored(prevNextContentState, TrailingSepError("sepBy", prevNextContentState.index, null))
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
            return errored(nextSeparatorState, NoMatchError("sepBy", nextSeparatorState.index))
        }

        return success(lastOkayState, results)
    }

    companion object {
        fun <NewT> commas(content: Parser<String, NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
            sepBy(content, -str(","), allowTrailingSep, requireMatch)

        fun <NewT> periods(content: Parser<String, NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
            sepBy(content, -str("."), allowTrailingSep, requireMatch)

        fun <NewT> whitespace(content: Parser<String, NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
            sepBy(content, whitespace, allowTrailingSep, requireMatch)

        fun <NewT> optionalWhitespace(content: Parser<String, NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
            sepBy(content, optionally(whitespace, ""), allowTrailingSep, requireMatch)
    }
}
