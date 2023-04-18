package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

// sigh
class sepBy<Target, NewT>(
    val content: Parser<Target, NewT>,
    val separator: Parser<Target, *>,
    val allowTrailingSep: Boolean = true,
    val requireMatch: Boolean = false
) : Parser<Target, List<NewT>>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out List<NewT>> {
        val results = mutableListOf<NewT>()

        var nextSeparatorState: ParseState<Target, *> = oldState
        lateinit var prevNextContentState: ParseState<Target, *>
        lateinit var nextContentState: ParseState<Target, *>

        lateinit var lastOkayState: ParseState<Target, *>

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
            sepBy(content, str(","), allowTrailingSep, requireMatch)

        fun <NewT> whitespaceInsensitiveCommas(content: Parser<String, NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
            sepBy(content, -str(","), allowTrailingSep, requireMatch)

        fun <NewT> whitespace(content: Parser<String, NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
            sepBy(content, whitespace, allowTrailingSep, requireMatch)

        fun <NewT> optionalWhitespace(content: Parser<String, NewT>, allowTrailingSep: Boolean = true, requireMatch: Boolean = false) =
            sepBy(content, optionally(whitespace, ""), allowTrailingSep, requireMatch)
    }
}
