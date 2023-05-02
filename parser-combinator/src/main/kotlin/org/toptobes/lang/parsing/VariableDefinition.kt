package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.oldlang.nodes.ByteArrayInstance
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.between
import org.toptobes.parsercombinator.impls.betweenSquareBrackets
import org.toptobes.parsercombinator.impls.pool
import org.toptobes.parsercombinator.impls.str

val variableDefinition = contextual { ctx ->
    val (isAllocated, isEmbedded, isImmediate) = ctx parse modifiers orCrash "Error reading modifiers"

    val type = ctx parse -identifier orFail  "Not a variable definition"
    val name = ctx parse -identifier orCrash "Error parsing var definition name"

    val definition = ctx parse when (type) {
        "byte", "db" -> byteDefinition(name)
        "word", "dw" -> wordDefinition(name)
        else -> typeDefinition(name)
    } orCrash "Error parsing $name's initializer"

    succeed(definition)
}

private val modifiers = contextual { ctx ->
    val modifiers = ctx parse pool(-str("alloc"), -str("embed"), -str("imm"), -str("export")) orCrash "Error parsing variable keywords"
    val isAllocated = "alloc" in modifiers || modifiers.isEmpty()
    val isEmbedded  = "embed" in modifiers
    val isImmediate = "imm"   in modifiers

    if ((isAllocated == isEmbedded) && (isAllocated xor isEmbedded xor isImmediate)) {
        crash("Conflicting allocation type keywords")
    }

    succeed(listOf(isAllocated, isEmbedded, isImmediate))
}

private fun wordDefinition(name: String): Parser<List<Definition>> {
    TODO()
}

private fun byteDefinition(name: String): Parser<List<Definition>> {
    TODO()
}

private fun typeDefinition(name: String): Parser<List<Definition>> {
    TODO()
}
