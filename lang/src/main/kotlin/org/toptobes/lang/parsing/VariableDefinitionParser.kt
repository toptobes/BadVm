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
    val isExport = ctx canParse -str("export")
    val name = ctx parse -strOf(identifier, ":") orFail "Not a label"
    val label = Label(name.trimEnd(':'), isExport)
    ctx.addLabel(label)
    succeed(label)
}

val variableDefinition = contextual {
    val (isAllocated, isExport) = ctx parse modifiers orCrash "Error reading modifiers"

    val type = ctx parse -identifier orFail  "Not a variable definition"
    val name = ctx parse -identifier orCrash "Error parsing var definition name"

    ctx parse -str("=") orCrash "Missing ="

    val (bytes, interp) = ctx parse when (type) {
        "byte", "db" -> byteConstructor(isAllocated)
        "word", "dw", "addr" -> wordConstructor(isAllocated)
        else -> typeConstructor(name, type, isAllocated)
    } orCrash "Error parsing $name's initializer"

    ctx.addVar(Variable(name, isExport, interp, bytes), isAllocated)
    succeed(DeleteThisNode)
}

private fun byteConstructor(isAllocated: Boolean) = any(
    singleByte.map { bytes ->
        bytes to if (isAllocated) Ptr(ByteIntrp) else ByteIntrp
    },
    byteArray.map { bytes ->
        bytes to if (isAllocated) Ptr(ByteIntrp) else Vec(ByteIntrp, bytes.size)
    },
)

private fun wordConstructor(isAllocated: Boolean) = any(
    singleWord.map { bytes ->
        bytes to if (isAllocated) Ptr(WordIntrp) else WordIntrp
    },
    wordArray.map { bytes ->
        bytes to if (isAllocated) Ptr(WordIntrp) else Vec(WordIntrp, bytes.size)
    },
)

private fun typeConstructor(name: String, type: String, isAllocated: Boolean) = typeConstructor(name, type).map { (bytes, type) ->
    bytes to if (isAllocated) Ptr(type) else type
}

private val modifiers = contextual {
    val modifiers = ctx parse pool(-str("alloc"), -str("imm"), -str("export")) orCrash "Error parsing variable keywords"
    val isEmbedded  = "imm"   in modifiers
    val isAllocated = "alloc" in modifiers || !isEmbedded
    val isExported  = "alloc" in modifiers || !isEmbedded

    if (isAllocated && isEmbedded) {
        crash("Conflicting allocation type keywords")
    }

    succeed(isAllocated to isExported)
}
