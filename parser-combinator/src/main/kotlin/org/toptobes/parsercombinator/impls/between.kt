package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

// sigh
class between<Target, NewT>(
    val left: Parser<Target, *>,
    val content: Parser<Target, NewT>,
    val right: Parser<Target, *> = left,
) : Parser<Target, NewT>() {
    override fun parse(oldState: ParseState<Target, *>): ParseState<Target, out NewT> {
        val leftState = left.parsePropagating(oldState)
        if (leftState.isErrored) {
            return errored(leftState, SequenceError("between", leftState.index, 0, leftState.error!!))
        }

        val targetState = content.parsePropagating(leftState)
        if (targetState.isErrored) {
            return errored(targetState, SequenceError("between", targetState.index, 1, targetState.error!!))
        }

        val rightState = right.parsePropagating(targetState)
        if (rightState.isErrored) {
            return errored(rightState, SequenceError("between", rightState.index, 2, rightState.error!!))
        }

        return success(rightState, targetState.result)
    }

    companion object {
        fun <NewT> squareBrackets(content: Parser<String, NewT>) =
            between(-str("["), content, -str("]"))

        fun <NewT> curlyBrackets(content: Parser<String, NewT>) =
            between(-str("{"), content, -str("}"))

        fun <NewT> parentheses(content: Parser<String, NewT>) =
            between(-str("("), content, -str(")"))

        fun <NewT> doubleQuotes(content: Parser<String, NewT>) =
            between(str("\""), content, str("\""))

        fun <NewT> singleQuotes(content: Parser<String, NewT>) =
            between(str("'"), content, str("'"))

        fun <NewT> whitespace(content: Parser<String, NewT>) =
            between(whitespace, content, whitespace)

        fun <NewT> optionalWhitespace(content: Parser<String, NewT>) =
            between(-whitespace, content, -whitespace)
    }
}
