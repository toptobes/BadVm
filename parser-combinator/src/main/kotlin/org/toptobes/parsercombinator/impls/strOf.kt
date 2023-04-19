@file:Suppress("ClassName")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.*

class strOf(vararg val parsers: Any) : Parser<String, String>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out String> {
        @Suppress("UNCHECKED_CAST")
        val actualParsers = parsers.map { when (it) {
            is String -> str(it)
            is Char   -> str(it)
            is Parser<*, *> -> it as Parser<String, String>
            else -> throw IllegalArgumentException("Non char/string/parser passed into strOf")
        }}

        return sequence(*actualParsers.toTypedArray())
            .map { it.joinToString("") }
            .parsePropagating(oldState)
    }
}
