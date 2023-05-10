package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.definitions.byteConstructor
import org.toptobes.lang.parsing.definitions.typeConstructor
import org.toptobes.lang.parsing.definitions.wordConstructor
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.pool
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.unaryMinus

val variableDefinition = contextual {
    val allocType = ctx parse modifiers orCrash "Error reading modifiers"

    val type = ctx parse -identifier orFail  "Not a variable definition"
    val name = ctx parse -identifier orCrash "Error parsing var definition name"

    ctx parse -str("=") orCrash "Missing ="

    val varDef = ctx parse when (type) {
        "byte", "db" -> byteConstructor(name, allocType)
        "word", "dw" -> wordConstructor(name, allocType)
        else -> typeConstructor(name, allocType)
    } orCrash "Error parsing $name's initializer"

    varDef.forEach(ctx::addVar)
    succeed(DeleteThisNode)
}

private val modifiers = contextual {
    val modifiers = ctx parse pool(-str("alloc"), -str("embed"), -str("imm"), -str("export")) orCrash "Error parsing variable keywords"
    val isAllocated = "alloc" in modifiers || modifiers.isEmpty()
    val isEmbedded  = "embed" in modifiers
    val isImmediate = "imm"   in modifiers

    if ((isAllocated == isEmbedded) && (isAllocated xor isEmbedded xor isImmediate)) {
        crash("Conflicting allocation type keywords")
    }

    val allocType = when {
        isEmbedded -> Embedded
        isImmediate -> Immediate
        else -> Allocated
    }

    succeed(allocType)
}
