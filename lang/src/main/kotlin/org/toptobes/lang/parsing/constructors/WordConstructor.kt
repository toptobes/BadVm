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

private val literalWordArray = cStyleArrayOf(
    singleWord
)..{ it.reduce { a, b -> a + b} }

private val wordArrayBuilder = contextual {
    val (n, init) = """
        word[n, init]
        [-] '['                      fail:  Not a word[] builder
        [*] \word                    crash: Can't parse the # of bytes needed
        [-] ','                      crash: word[] builder missing comma
        [*] '?' | 'it' | \word       crash: Can't parse word[] initializer
        [-] ']'                      crash: word[] builder missing closing bracket
    """.compilePc()(ctx)

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
