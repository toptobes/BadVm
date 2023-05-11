package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.*
import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun byteConstructor(name: String, allocType: AllocationType) = lazy {
    any(byteArray(name, allocType), singleByte(name, allocType))
}

private fun singleByte(name: String, allocType: AllocationType) = contextual {
    val byte = ctx.parse(byte) orFail "Not a single byte"

    val definition = when (allocType) {
        Allocated -> {
            val handle = ctx allocBytes byteArrayOf(byte)
            Variable(name, PromisedBytes(handle) { Ptr<ByteInterpretation>() })
        }
        Immediate -> {
            Constant(name, ImmediateBytes(byteArrayOf(byte)) { ByteInterpretation })
        }
    }

    succeed(definition)
}

private fun byteArray(name: String, allocType: AllocationType) = contextual {
    val bytes = ctx parse any(byteArrayBuilder, literalByteArray, string) orFail "Not a byte array"

    val definition = when (allocType) {
        Allocated -> {
            val handle = ctx allocBytes bytes
            Variable(name, PromisedBytes(handle) { Ptr<ByteInterpretation>() })
        }
        Immediate -> {
            Constant(name, ImmediateBytes(bytes) { Vec<ByteInterpretation>(bytes.size) })
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
