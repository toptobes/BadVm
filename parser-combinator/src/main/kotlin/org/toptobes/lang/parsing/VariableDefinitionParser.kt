package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.definitions.byteConstructor
import org.toptobes.lang.parsing.definitions.typeConstructor
import org.toptobes.lang.parsing.definitions.wordConstructor
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.pool
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.unaryMinus
import kotlin.contracts.contract

val variableDefinition = contextual {
    val allocType = ctx parse modifiers orCrash "Error reading modifiers"

    val type = ctx parse -identifier orFail  "Not a variable definition"
    val name = ctx parse -identifier orCrash "Error parsing var definition name"

    ctx parse -str("=") orCrash "Missing ="

    val (constructor, bytes) = ctx parse when (type) {
        "byte", "db" -> byteConstructor(allocType)
        "word", "dw" -> wordConstructor(allocType)
        else -> typeConstructor(name, allocType)
    } orCrash "Error parsing $name's initializer"

    ctx addVar constructor(name)
    succeed(DeleteThisNode)
}

private val modifiers = contextual {
    val modifiers = ctx parse pool(-str("alloc"), -str("imm"), -str("export")) orCrash "Error parsing variable keywords"
    val isEmbedded  = "embed" in modifiers
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

fun makeVariable(bytes: PromisedBytes): (String) -> Variable {
    return { name: String -> Variable(name, bytes) }
}

fun makeConstant(bytes: ImmediateBytes): (String) -> Constant {
    return { name: String -> Constant(name, bytes) }
}
