package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.*
import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun byteConstructor(allocType: AllocationType) = lazy {
    any(byteArray(allocType), singleByte(allocType))
}

private fun singleByte(allocType: AllocationType) = contextual {
    val rawByte = ctx parse byte orFail "Not a single byte"

    val definition = when (allocType) {
        Allocated -> {
            val handle = ctx allocBytes byteArrayOf(rawByte)
            val bytes = PromisedBytes(handle) { Ptr<ByteInterpretation>() }
            makeVariable(bytes) to bytes
        }
        Immediate -> {
            val bytes = ImmediateBytes(byteArrayOf(rawByte)) { ByteInterpretation };
            makeConstant(bytes) to bytes
        }
    }

    succeed(definition)
}

private fun byteArray(allocType: AllocationType) = contextual {
    val rawBytes = ctx parse any(byteArrayBuilder, literalByteArray, string) orFail "Not a byte array"

    val definition = when (allocType) {
        Allocated -> {
            val handle = ctx allocBytes rawBytes
            val bytes = PromisedBytes(handle) { Ptr<ByteInterpretation>() }
            makeVariable(bytes) to bytes
        }
        Immediate -> {
            val bytes = ImmediateBytes(rawBytes) { Vec<ByteInterpretation>(rawBytes.size) }
            makeConstant(bytes) to bytes
        }
    }

    succeed(definition)
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
