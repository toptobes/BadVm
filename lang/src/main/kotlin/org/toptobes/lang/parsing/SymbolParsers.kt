@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.*
import org.toptobes.parsercombinator.ParsingException
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.unaryMinus

val mem = contextual {
    ctx parse str("@") orFail "Not mem usage"
    val result = ctx parse any(constPtr, varPtr) orCrash "Failed parsing mem usage"
    succeed(result)
}

val label = contextual {
    val name = ctx parse identifier orFail "Not a label usage"
    ctx parse whitespace orFail "Not a label usage"
    val symbol = ctx.lookup<Label>(name) orFail "$name is not a label"
    succeed(symbol)
}

val addr = contextual {
    ctx parse str("&") orFail "Not byte embedding"

    val (variable, names) = ctx parse variableAndFields orCrash "Error parsing fields for addr"

    if (variable.intrp !is Ptr) {
        crash("Trying to get addr of non-var")
    }

    val addr = when (variable.intrp.intrp) {
        is TypeIntrp -> variable.bytes.toWord() + getFieldOrTypeOffset(names, variable.intrp.intrp)
        else -> variable.bytes.toWord()
    }.toBytes()

    succeed(addr)
}

fun embeddedBytes(size: IntRange = 0..Word.MAX_VALUE, requireEven: Boolean = false) = contextual {
    ctx parse str("...") orFail "Not byte embedding"

    val (variable, names) = ctx parse variableAndFields orCrash "Error parsing fields for embedded bytes"

    val bytes = when (variable.intrp) {
        is TypeIntrp -> {
            val field = getField(names, variable.intrp)
            variable.bytes.copyFromPlus(field.offset, field.size)
        }
        else ->  variable.bytes
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
    val (variable, names) = ctx parse variableAndFields orFail "Error parsing fields for const"

    val bytes = when (variable.intrp) {
        is ByteIntrp -> variable.bytes
        is WordIntrp -> variable.bytes
        is TypeIntrp -> {
            val field = getField(names, variable.intrp)
            variable.bytes.copyFromPlus(field.offset, field.size)
        }
        else -> crash("Illegal const usage")
    }

    if (bytes.size != size) {
        crash("Expected size $size, got ${bytes.size}")
    }

    succeed(bytes)
}

private val constPtr = contextual {
    val ptrIntrp = ctx parse castStart orFail "No cast"

    if (ptrIntrp !is Ptr) {
        crash("Expected ptr, got ${ptrIntrp.prettyString()}")
    }

    ctx.parse(word) {
        ctx parse -str(">") orCrash "Cast missing >"
        succeed(ptrIntrp to it.toBytes())
    }

    val (variable, names) = ctx parse variableAndFields orCrash "Error parsing fields for const ptr"

    ctx parse -str(">") orCrash "Cast missing >"

    val addr = when (variable.intrp) {
        is TypeIntrp -> variable.bytes.copyFromPlus(getFieldOrTypeOffset(names, variable.intrp), 2)
        is WordIntrp -> variable.bytes
        else -> crash("Can not cast non-word-sized const to ptr")
    }

    succeed(addOffsetToAddr(addr, ptrIntrp, names))
}

private val varPtr = contextual {
    val (variable, names) = ctx parse variableAndFields orCrash "Error parsing name/fields for var ptr"
    succeed(addOffsetToAddr(variable.bytes, variable.intrp as Ptr, names))
}

private fun addOffsetToAddr(currentAddr: ByteArray, ptr: Ptr, names: List<String>): Pair<Ptr, ByteArray> {
    val ptrInterpretation = ptr.intrp

    val (addrIntrp, addr) = when (ptrInterpretation) {
        is TypeIntrp -> getAddrAtOffset(names, ptrInterpretation, currentAddr.toWord())
        else -> ptrInterpretation to currentAddr
    }

    return Ptr(addrIntrp) to addr
}

private fun getAddrAtOffset(names: List<String>, type: TypeIntrp, addr: Word): Pair<Interpretation, ByteArray> {
    val field = getField(names, type)
    return field.intrp to (addr + field.offset).toBytes()
}

private fun getField(names: List<String>, type: TypeIntrp): Field<*> {
    val name = names.firstOrNull()
        ?: throw ParsingException("Can not resolve type field; field chain ends @ type ${type.name}")

    val field = type.fields[name]
        ?: throw ParsingException("$name is an invalid field for type ${type.name}")

    return when (field.intrp) {
        is TypeIntrp -> getField(names.drop(1), type)
        else -> field
    }
}

fun getFieldOrTypeOffset(names: List<String>, type: TypeIntrp): Int {
    val name = names.firstOrNull()
        ?: return 0

    val field = type.fields[name]
        ?: throw ParsingException("$name is an invalid field for type ${type.name}")

    return when (field.intrp) {
        is TypeIntrp -> {
            val offset = getFieldOrTypeOffset(names.drop(1), type)
            if (offset == 0) field.offset else offset
        }
        else -> field.offset
    }
}

private val variableAndFields = contextual {
    val names = ctx parse -sepByPeriods(-identifier, requireMatch = true) orFail "Missing identifier"

    val variable = ctx.lookup<Variable>(names[0]) ?: crash("${names[0]} is an invalid symbol")
    succeed(variable to names.drop(1))
}

private fun ByteArray.copyFromPlus(from: Int, plus: Int): ByteArray {
    return copyOfRange(from, from + plus)
}
