@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun <R> between(left: Parser<*>, content: Parser<R>, right: Parser<*> = left) = Parser { oldState ->
    val leftState = left.parsePropagating(oldState)
    if (leftState.isErrored()) {
        return@Parser errored(leftState, "between: Could not match opening parser (${leftState.error})")
    }

    val targetState = content.parsePropagating(leftState)
    if (targetState.isErrored()) {
        return@Parser errored(leftState, "between: Could not match content parser (${targetState.error})")
    }

    val rightState = right.parsePropagating(targetState)
    if (rightState.isErrored()) {
        return@Parser errored(leftState, "between: Could not match closing parser (${rightState.error})")
    }

    return@Parser success(rightState, targetState.result)
}

fun <T> betweenSquareBrackets(content: Parser<T>) =
    between(-str("["), content, -str("]"))

fun <T> betweenCurlyBrackets(content: Parser<T>) =
    between(-str("{"), content, -str("}"))

fun <T> betweenParentheses(content: Parser<T>) =
    between(-str("("), content, -str(")"))

fun <T> betweenAngleBrackets(content: Parser<T>) =
    between(-str("<"), content, -str(">"))

fun <T> betweenDoubleQuotes(content: Parser<T>) =
    between(str("\""), content, str("\""))

fun <T> betweenSingleQuotes(content: Parser<T>) =
    between(str("'"), content, str("'"))

fun <T> betweenWhitespace(content: Parser<T>) =
    between(-whitespace, content, -whitespace)
