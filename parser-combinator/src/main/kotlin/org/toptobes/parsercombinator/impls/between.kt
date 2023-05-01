@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> between(left: Parser<*>, content: Parser<R>, right: Parser<*> = left) = Parser { oldState ->
    val leftState = left.parsePropagating(oldState)
    if (leftState.isErrored) {
        return@Parser errored(leftState, SequenceError("between", leftState.index, 0, leftState.error!!))
    }

    val targetState = content.parsePropagating(leftState)
    if (targetState.isErrored) {
        return@Parser errored(targetState, SequenceError("between", targetState.index, 1, targetState.error!!))
    }

    val rightState = right.parsePropagating(targetState)
    if (rightState.isErrored) {
        return@Parser errored(rightState, SequenceError("between", rightState.index, 2, rightState.error!!))
    }

    return@Parser succeed(rightState, targetState.result)
}

fun <NewT> betweenSquareBrackets(content: Parser<NewT>) =
    between(-str("["), content, -str("]"))

fun <NewT> betweenCurlyBrackets(content: Parser<NewT>) =
    between(-str("{"), content, -str("}"))

fun <NewT> betweenParentheses(content: Parser<NewT>) =
    between(-str("("), content, -str(")"))

fun <NewT> betweenDoubleQuotes(content: Parser<NewT>) =
    between(str("\""), content, str("\""))

fun <NewT> betweenSingleQuotes(content: Parser<NewT>) =
    between(str("'"), content, str("'"))

fun <NewT> betweenWhitespace(content: Parser<NewT>) =
    between(whitespace, content, whitespace)

fun <NewT> betweenOptionalWhitespace(content: Parser<NewT>) =
    between(-whitespace, content, -whitespace)
