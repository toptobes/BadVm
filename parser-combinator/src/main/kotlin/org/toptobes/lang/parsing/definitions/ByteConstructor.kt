@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.byte
import org.toptobes.lang.parsing.cStyleArrayOf
import org.toptobes.lang.parsing.word
import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun byteConstructor(name: String, allocType: AllocationType) = lazy {
    any(byteArray(name, allocType), singleByte(name, allocType))
}

private fun singleByte(name: String, allocType: AllocationType) = contextual {
    val byte = ctx.parse(byte) orFail "Not a single byte"
    succeed(listOf(VarDefinition(name, listOf(byte), allocType, OperandType<Imm8>())))
}

private fun byteArray(name: String, allocType: AllocationType) = contextual {
    ctx.parse(byteArrayBuilder) {
        succeed(listOf(VarDefinition(name, it, allocType, Bytes)))
    }

    val bytes = ctx parse any(literalByteArray, string) orFail "Not a byte array"
    succeed(listOf(VarDefinition(name, bytes, allocType, Bytes)))
}

private val literalByteArray = cStyleArrayOf(any(
    byte..(::listOf)
))..{ it.flatten() }

private val byteArrayBuilder = contextual {
    val numBytes = word..(Word::toString)
    val initializer = (byte..(Byte::toString) or str("it") or str("?"))

    ctx parse str("[")               orFail  "Not a byte array builder"
    val n = ctx parse numBytes       orCrash "Can't parse numBytes (${ctx.errorStr})"
    ctx parse -str(",")              orCrash "Builder missing comma"
    val init = ctx parse initializer orCrash "Can't parse initializer (${ctx.errorStr})"
    ctx parse str("]")               orCrash "Byte array builder doesn't have closing ]"

    val initByte = when {
        init.isByte() -> init.toByteOrNull()
        init == "it"  -> null
        init == "?"   -> 0
        else -> crash("Invalid initializer ($init) in byte array builder")
    }

    val bytes = List(n.toInt()) { initByte ?: it.toByte() }
    succeed(bytes)
}

private val string = betweenDoubleQuotes(until(char) { it.ifOkay { result == '"' } ?: false })
    .map { it.map { chr -> chr.code.toByte() } }

private fun String.isByte() = toByteOrNull() != null
