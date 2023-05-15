package org.toptobes.lang.parsing.constructors

import org.toptobes.lang.parsing.*
import org.toptobes.lang.utils.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

val singleWord = contextual {
    val bytes = ctx parse -any(
        word..{ it.toBytes() },
        embeddedBytes(2..2),
        const(2),
        addr,
    ) orFail "Not a single word"

    succeed(bytes)
}

val wordArray: Parser<ByteArray> get() = contextual {
    val bytes = ctx parse -any(
        wordArrayBuilder,
        literalWordArray,
        string,
        embeddedBytes(requireEven = true),
        singleWord,
    ) orFail "Not a word array"

    val moreBytes = ctx.parse(-(str(",") or str("++"))) {
        ctx parse -wordArray orCrash "Error parsing word[] after concat"
    } ?: ByteArray(0)

    succeed(bytes + moreBytes)
}

private val literalWordArray = cStyleArrayOf(any(
    singleWord
))..{ it.reduce { a, b -> a + b} }

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
    succeed(words.toBytes())
}

private val string = betweenDoubleQuotes(until(char) { it.ifOkay { result == '"' } ?: false })
    .map { it.map { chr -> chr.code.toWord() }.toWordArray().toBytes() }

private fun String.isWord() = toWordOrNull() != null
