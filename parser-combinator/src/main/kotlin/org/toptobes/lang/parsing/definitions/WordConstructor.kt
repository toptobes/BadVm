package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.*
import org.toptobes.lang.utils.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun wordConstructor(name: String, allocType: AllocationType) = lazy {
    any(wordArray(name, allocType), singleWord(name, allocType))
}

private fun singleWord(name: String, allocType: AllocationType) = contextual {
    val word = ctx.parse(word) orFail "Not a single word"
    val bytes = word.toBytes()

    val definition = when (allocType) {
        Allocated -> {
            val handle = ctx allocBytes bytes
            Variable(name, PromisedBytes(handle) { Ptr<WordInterpretation>() })
        }
        Immediate -> {
            Constant(name, ImmediateBytes(bytes) { WordInterpretation })
        }
    }

    succeed(definition)
}

private fun wordArray(name: String, allocType: AllocationType) = contextual {
    val words = ctx parse any(wordArrayBuilder, literalWordArray, string) orFail "Not a word array"
    val bytes = words.toBytes()

    val definition = when (allocType) {
        Allocated -> {
            val handle = ctx allocBytes bytes
            Variable(name, PromisedBytes(handle) { Ptr<WordInterpretation>() })
        }
        Immediate -> {
            Constant(name, ImmediateBytes(bytes) { Vec<WordInterpretation>(bytes.size) })
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
