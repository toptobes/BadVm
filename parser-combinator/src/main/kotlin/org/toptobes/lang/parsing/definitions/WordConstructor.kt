package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.*
import org.toptobes.lang.utils.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun wordConstructor(allocType: AllocationType) = lazy {
    any(wordArray(allocType), singleWord(allocType))
}

private fun singleWord(allocType: AllocationType) = contextual {
    val rawWord = ctx parse word orFail "Not a single word"
    val rawBytes = rawWord.toBytes()

    val definition = when (allocType) {
        Allocated -> {
            val handle = ctx allocBytes rawBytes
            val bytes = PromisedBytes(handle) { Ptr<WordInterpretation>() }
            makeVariable(bytes) to bytes
        }
        Immediate -> {
            val bytes = ImmediateBytes(rawBytes) { WordInterpretation }
            makeConstant(bytes) to bytes
        }
    }

    succeed(definition)
}

private fun wordArray(allocType: AllocationType) = contextual {
    val rawWords = ctx parse any(wordArrayBuilder, literalWordArray, string) orFail "Not a word array"
    val rawBytes = rawWords.toBytes()

    val definition = when (allocType) {
        Allocated -> {
            val handle = ctx allocBytes rawBytes
            val bytes = PromisedBytes(handle) { Ptr<WordInterpretation>() }
            makeVariable(bytes) to bytes
        }
        Immediate -> {
            val bytes = ImmediateBytes(rawBytes) { Vec<WordInterpretation>(rawBytes.size) }
            makeConstant(bytes) to bytes
        }
    }

    succeed(definition)
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
