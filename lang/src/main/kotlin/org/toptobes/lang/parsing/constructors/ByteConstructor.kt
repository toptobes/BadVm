package org.toptobes.lang.parsing.constructors

import org.toptobes.lang.parsing.*
import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

val singleByte = contextual {
    val bytes = ctx parse -any(
        byte..{ byteArrayOf(it) },
        embeddedBytes(1..1),
        const(1),
    ) orFail "Not a single byte"

    succeed(bytes)
}

val byteArray: Parser<ByteArray> get() = contextual {
    val bytes = ctx parse -any(
        byteArrayBuilder,
        literalByteArray,
        string,
        embeddedBytes(),
        singleByte,
    ) orFail "Not a byte array"

    val moreBytes = ctx.parse(-str(",")) {
        ctx parse -byteArray orCrash "Error parsing byte[] after concat"
    } ?: ByteArray(0)

    succeed(bytes + moreBytes)
}

private val literalByteArray = cStyleArrayOf(any(
    singleByte
))..{ it.reduce { a, b -> a + b} }

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
