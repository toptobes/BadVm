@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.prettyString
import org.toptobes.lang.utils.toBytes
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.ParsingException
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.or
import org.toptobes.parsercombinator.unaryMinus

val mem = contextual {
    ctx.parse(-label) {
        succeed(it)
    }

    ctx parse str("@") orFail "Not mem usage"
    val result = ctx parse any(constPtr, varPtr) orCrash "Failed parsing mem usage"
    succeed(result)
}

private val label = contextual {
    val name = ctx parse identifier orFail "Not a label"
    val symbol = ctx.state.vars[name]

    if (symbol !is Label) {
        fail("$name is not a label")
    }

    succeed(Ptr(WordInterpretation) to symbol.address.toBytes())
}

val addr = contextual {
    ctx parse str("&") orFail "Not byte embedding"

    val (names, symbol, interpretation) = ctx parse initialSymbolAndFields orCrash "Error parsing fields for addr"

    if (symbol !is Variable) {
        crash("Trying to get addr of non-var")
    }
    interpretation as Ptr

    val addr = when (interpretation.interpretation) {
        is TypeInterpretation -> symbol.address + getFieldOrTypeOffset(names.drop(1), interpretation.interpretation)
        else -> symbol.address
    }.toBytes()
    succeed(addr)
}

fun embeddedBytes(size: IntRange = 0..Int.MAX_VALUE, requireEven: Boolean = false) = contextual {
    ctx parse str("...") orFail "Not byte embedding"

    val (names, symbol, interpretation) = ctx parse initialSymbolAndFields orCrash "Error parsing fields for embedded bytes"

    if (symbol !is Constant) {
        crash("Trying to spread non-const")
    }

    val bytes = when (interpretation) {
        is ByteInterpretation -> symbol.bytes
        is WordInterpretation -> symbol.bytes
        is TypeInterpretation -> bytesAtField(names, interpretation, symbol.bytes, size)
        is Vec -> symbol.bytes
        else -> crash("Illegal spread")
    }

    if (requireEven && bytes.size % 2 != 0) {
        crash("Expected bytes to be aligned to 2, got size ${bytes.size}")
    }

    if (bytes.size !in size) {
        crash("Expected field size $size, got ${bytes.size}")
    }

    succeed(bytes)
}

fun const(size: Int) = contextual {
    val (names, symbol, interpretation) = ctx parse initialSymbolAndFields orFail "Error parsing fields for const"

    if (symbol !is Constant) {
        fail("Non-const")
    }

    val bytes = when (interpretation) {
        is ByteInterpretation -> symbol.bytes
        is WordInterpretation -> symbol.bytes
        is TypeInterpretation -> bytesAtField(names, interpretation, symbol.bytes, 1..2)
        else -> crash("Illegal const usage")
    }

    if (bytes.size != size) {
        crash("Expected size $size, got ${bytes.size}")
    }

    succeed(bytes)
}

private val constPtr = contextual {
    val interpretation = ctx parse (str("word") or str("byte")) orCrash "Missing ptr type (word | byte)"
    val ptrInterpretation = Ptr(if (interpretation == "byte") ByteInterpretation else WordInterpretation)

    ctx parse -str("ptr") orCrash "Missing 'ptr' after $interpretation"

    ctx.parse(word) {
        succeed(ptrInterpretation to it.toBytes())
    }

    val (names, symbol, symbolInterpretation) = ctx parse initialSymbolAndFields orCrash "Error parsing fields for const ptr"

    if (symbol !is Constant) {
        crash("Identifier is not a constant")
    }

    val bytes = when (symbolInterpretation) {
        is WordInterpretation -> symbol.bytes
        is TypeInterpretation -> bytesAtField(names, symbolInterpretation, symbol.bytes, 2..2)
        else -> crash("Illegal deref of ${symbolInterpretation.prettyString()}")
    }

    succeed(ptrInterpretation to bytes)
}.let { between(str("{"), -it, -str("}")) }

private val varPtr = contextual {
    val (names, symbol, interpretation) = ctx parse initialSymbolAndFields orCrash "Error parsing fields for var ptr"

    if (symbol !is Variable) {
        crash(if (symbol is Label) "Can't deref label" else "Use @{(word|byte) ptr ...} syntax instead")
    }

    interpretation as Ptr

    val (addrIntrp, addr) = when (interpretation.interpretation) {
        is TypeInterpretation -> getAddrAtOffset(names.drop(1), interpretation.interpretation, symbol.address)
        else -> interpretation.interpretation to symbol.address.toBytes()
    }

    succeed(Ptr(addrIntrp) to addr)
}

private fun bytesAtField(names: List<String>, type: TypeInterpretation, bytes: ByteArray, size: IntRange): ByteArray {
    val field = getField(names.drop(1), type)

    if (field.size !in size) {
        crash("Expected field size $size, got ${field.size}")
    }

    return bytes.copyOfRange(field.offset, field.offset + field.size)
}

private fun getAddrAtOffset(names: List<String>, type: TypeInterpretation, addr: Word): Pair<Interpretation, ByteArray> {
    val field = getField(names, type)
    return field.interpretation to (addr + field.offset).toBytes()
}

private fun getField(names: List<String>, type: TypeInterpretation): Field<*> {
    val name = names.firstOrNull()
        ?: throw ParsingException("Can not resolve type field; field chain ends @ type ${type.typeName}")

    val field = type.fields[name]
        ?: throw ParsingException("$name is an invalid field for type ${type.typeName}")

    return when (field.interpretation) {
        is TypeInterpretation -> getField(names.drop(1), type)
        else -> field
    }
}

private fun getFieldOrTypeOffset(names: List<String>, type: TypeInterpretation): Int {
    val name = names.firstOrNull()
        ?: return 0

    val field = type.fields[name]
        ?: throw ParsingException("$name is an invalid field for type ${type.typeName}")

    return when (field.interpretation) {
        is TypeInterpretation -> {
            val offset = getFieldOrTypeOffset(names.drop(1), type)
            if (offset == 0) field.offset else offset
        }
        else -> field.offset
    }
}

private val initialSymbolAndFields = contextual {
    val names = ctx parse -sepByPeriods(-identifier, requireMatch = true) orFail "Missing identifier"

    val symbol = ctx.state.vars[names[0]] ?: crash("${names[0]} is an invalid symbol")
    val interpretation = ctx.state.assumptions[names[0]]!!

    succeed(Triple(names, symbol, interpretation))
}
