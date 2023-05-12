package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.WordInterpretation
import org.toptobes.lang.parsing.cStyleArrayOf
import org.toptobes.lang.parsing.word
import org.toptobes.lang.utils.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

val singleWord = contextual {
    val word = ctx parse word orFail "Not a single word"
    val bytes = word.toBytes()
    succeed(bytes)
}

val wordArray = contextual {
    val words = ctx parse any(wordArrayBuilder, literalWordArray, string) orFail "Not a word array"
    val bytes = words.toBytes()
    succeed(bytes)
}

private val literalWordArray = cStyleArrayOf(any(
    word
))..{ it.toWordArray() }

private val wordArrayBuilder = contextual {
    val numBytes = word..(Word::toString)
    val initializer = (word..(Word::toString) or str("it") or str("?"))

    val (n, _, init) = ctx parse betweenSquareBrackets(sequence(numBytes, -str(","), initializer)) orFail "Not a word array builder"

    val initWord = when {
        init.isWord() -> init.toWordOrNull()
        init == "it"  -> null
        init == "?"   -> 0
        else -> crash("Invalid initializer ($init) in word array builder")
    }

    val words = WordArray(n.toInt()) { initWord ?: it.toWord() }
    succeed(words)
}

private val string = betweenDoubleQuotes(until(char) { it.ifOkay { result == '"' } ?: false })
    .map { it.map { chr -> chr.code.toWord() }.toWordArray() }

private fun String.isWord() = toWordOrNull() != null
