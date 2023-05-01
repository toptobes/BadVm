package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

fun strOf(vararg parsers: Any) = Parser { oldState ->
    val actualParsers = parsers.map { when (it) {
            is String -> str(it)
            is Char   -> str(it)
            is Parser<*> -> it
            else -> throw IllegalArgumentException("Non char/string/parser passed into strOf")
    } }

    sequence(*actualParsers.toTypedArray())
        .map { it.joinToString("") }
        .parsePropagating(oldState)
}
