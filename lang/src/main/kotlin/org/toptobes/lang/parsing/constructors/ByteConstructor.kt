package org.toptobes.lang.parsing.constructors

import org.toptobes.lang.parsing.*
import org.toptobes.lang.utils.Abs8
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

val singleByte = contextual {
    val (bytes, isAbs) = ctx parse -any(
        litByte..{ byteArrayOf(it) to Abs8 },
        embeddedBytes(1..1),
        const(1),
    ) orFail "Not a single byte"

    succeed(bytes to isAbs)
}

val byteArray: Parser<ByteArray> get() = contextual {
    val bytes = ctx parse -any(
        byteArrayBuilder,
        literalByteArray,
        string,
        embeddedBytes(),
        singleByte,
    ) orFail "Not a byte array"

    val moreBytes = ctx.parse(-(str(",") or str("++"))) {
        ctx parse -byteArray orCrash "Error parsing byte[] after concat"
    } ?: ByteArray(0)

    succeed(bytes + moreBytes)
}

private val literalByteArray = cStyleArrayOf(
    singleByte
)..{ it.reduce { a, b -> a + b} }

private val byteArrayBuilder = contextual {
    val (n, init) = """
        byte[n, init]
        [-] '['                      fail:  Not a byte[] builder
        [*] \word                    crash: Can't parse the # of bytes needed
        [-] ','                      crash: byte[] builder missing comma
        [*] '?' | 'it' | \byte       crash: Can't parse byte[] initializer
        [-] ']'                      crash: byte[] builder missing closing bracket
    """.compilePc()(ctx)

    val initByte = when {
        init.isByte() -> init.toByteOrNull()
        init == "it"  -> null
        init == "?"   -> 0
        else -> crash("Invalid initializer ($init) in byte array builder")
    }

    val bytes = ByteArray(n.toInt()) { initByte ?: it.toByte() }
    succeed(bytes to IS_ABS)
}

private val string = betweenDoubleQuotes(until(char) { it.ifOkay { result == '"' } ?: false })
    .map { it.map { chr -> chr.code.toByte() }.toByteArray() to IS_ABS }

private fun String.isByte() = toByteOrNull() != null
