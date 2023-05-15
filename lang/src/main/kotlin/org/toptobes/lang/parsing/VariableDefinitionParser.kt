@file:Suppress("ArrayInDataClass")

package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.constructors.*
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.pool
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.impls.strOf
import org.toptobes.parsercombinator.unaryMinus

val labelDefinition = contextual {
    val name = ctx parse -strOf(identifier, ":") orFail "Not a label"
    val label = Label(name.trimEnd(':'))
    succeed(label)
}

val variableDefinition = contextual {
    val allocType = ctx parse modifiers orCrash "Error reading modifiers"

    val type = ctx parse -identifier orFail  "Not a variable definition"
    val name = ctx parse -identifier orCrash "Error parsing var definition name"

    ctx parse -str("=") orCrash "Missing ="

    val (bytes, interpretation) = ctx parse when (type) {
        "byte", "db" -> byteConstructor(allocType)
        "word", "dw", "addr" -> wordConstructor(allocType)
        else -> typeConstructor(name, type, allocType)
    } orCrash "Error parsing $name's initializer"

    val symbol = when (allocType) {
        Allocated -> Variable(name, bytes)
        Immediate -> Constant(name, bytes)
    }

    ctx addVar symbol
    ctx.assume(name, interpretation)
    succeed(DeleteThisNode)
}

private fun byteConstructor(allocType: AllocationType) = any(
    singleByte.map { bytes ->
        bytes to if (allocType == Allocated) Ptr(ByteInterpretation) else ByteInterpretation
    },
    byteArray.map { bytes ->
        bytes to if (allocType == Allocated) Ptr(ByteInterpretation) else Vec(ByteInterpretation, bytes.size)
    },
)

private fun wordConstructor(allocType: AllocationType) = any(
    singleWord.map { bytes ->
        bytes to if (allocType == Allocated) Ptr(WordInterpretation) else WordInterpretation
    },
    wordArray.map { bytes ->
        bytes to if (allocType == Allocated) Ptr(WordInterpretation) else Vec(WordInterpretation, bytes.size)
    },
)

private fun typeConstructor(name: String, type: String, allocType: AllocationType) = typeConstructor(name, type).map { (bytes, type) ->
    bytes to if (allocType == Allocated) Ptr(type) else type
}

private val modifiers = contextual {
    val modifiers = ctx parse pool(-str("alloc"), -str("imm"), -str("export")) orCrash "Error parsing variable keywords"
    val isEmbedded  = "imm"   in modifiers
    val isAllocated = "alloc" in modifiers || !isEmbedded

    if (isAllocated && isEmbedded) {
        crash("Conflicting allocation type keywords")
    }

    val allocType = when {
        isEmbedded -> Immediate
        else -> Allocated
    }

    succeed(allocType)
}

sealed interface AllocationType
object Allocated : AllocationType
object Immediate : AllocationType

fun makeVariable(bytes: ByteArray): (String) -> Variable {
    return { name: String -> Variable(name, bytes) }
}

fun makeConstant(bytes: ByteArray): (String) -> Constant {
    return { name: String -> Constant(name, bytes) }
}
