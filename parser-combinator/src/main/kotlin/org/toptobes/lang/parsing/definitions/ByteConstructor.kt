package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.parsing.byte
import org.toptobes.lang.parsing.cStyleArrayOf
import org.toptobes.lang.parsing.identifier
import org.toptobes.lang.parsing.word
import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

val singleByte = contextual {
    val bytes = ctx parse any(byte..{ byteArrayOf(it) }) orFail "Not a single byte"
    succeed(bytes)
}

val byteArray = contextual {
    val bytes = ctx parse any(byteArrayBuilder, literalByteArray, string, embeddedBytes) orFail "Not a byte array"
    succeed(bytes)
}

private val embeddedBytes = contextual {
    ctx parse -str("...") orFail "Not byte embedding"
    val name = ctx parse -identifier orCrash "Not an identifier"
    val symbol = ctx.state.vars[name] orCrash "$name is not a valid identifier"
    succeed(symbol.bytes)
}

private val literalByteArray = cStyleArrayOf(any(
    byte
))..{ it.toByteArray() }

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

    val bytes = ByteArray(n.toInt()) { initByte ?: it.toByte() }
    succeed(bytes)
}

private val string = betweenDoubleQuotes(until(char) { it.ifOkay { result == '"' } ?: false })
    .map { it.map { chr -> chr.code.toByte() }.toByteArray() }

private fun String.isByte() = toByteOrNull() != null
