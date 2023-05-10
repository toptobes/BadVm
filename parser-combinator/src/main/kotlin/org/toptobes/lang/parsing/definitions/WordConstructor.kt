@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.cStyleArrayOf
import org.toptobes.lang.parsing.word
import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.toBytes
import org.toptobes.lang.utils.toWord
import org.toptobes.lang.utils.toWordOrNull
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun wordConstructor(name: String, allocType: AllocationType) = lazy {
    any(wordArray(name, allocType), singleWord(name, allocType))
}

private fun singleWord(name: String, allocType: AllocationType) = contextual {
    val word = ctx.parse(word) orFail "Not a single word"

    val bytes = listOf(word).flatMap(Word::toBytes)
    succeed(listOf(VarDefinition(name, bytes, allocType, OperandType<Imm16>())))
}

private fun wordArray(name: String, allocType: AllocationType) = contextual {
    ctx.parse(wordArrayBuilder) {
        val bytes = it.flatMap(Word::toBytes)
        succeed(listOf(VarDefinition(name, bytes, allocType, Words)))
    }

    val words = ctx parse any(literalWordArray, string) orFail "Not a word array"
    val bytes = words.flatMap(Word::toBytes)
    succeed(listOf(VarDefinition(name, bytes, allocType, Words)))
}

private val literalWordArray = cStyleArrayOf(any(
    word..(::listOf)
))..{ it.flatten() }

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

    val words = List(n.toInt()) { initWord ?: it.toWord() }
    succeed(words)
}

private val string = betweenDoubleQuotes(until(char) { it.ifOkay { result == '"' } ?: false })
    .map { it.map { chr -> chr.code.toWord() } }

private fun String.isWord() = toWordOrNull() != null
