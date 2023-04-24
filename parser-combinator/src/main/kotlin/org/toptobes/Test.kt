package org.toptobes

import org.toptobes.lang.nodes.*
import org.toptobes.lang.parsers.identifier
import org.toptobes.lang.utils.StatefulParsingException
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.sepBy
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.unaryMinus

fun main() {
    val H = DefinedType("H", listOf(TypeDefinitionFieldByte("s")))
    val h = TypeInstance("H", H, listOf(ByteVarDefinition("s", 1)))

    val vars = MutNodes().apply {
        typeDefs += H
        varDefs  += h
    }

    try {
        println(varUsage(vars)("@h.s"))
    } catch (e: StatefulParsingException) {
        println(e.message!!)
    }
}

fun varUsage(nodes: Nodes) = contextual { ctx ->
    val isDeref = ctx canTryParse -str('@')

    val cascadingNames = ctx parse sepBy.periods(-identifier, allowTrailingSep = false) or ccrash("No identifier found for variable usage")
    val firstName = cascadingNames[0]
    val first = nodes.varDefs[firstName]                                                or ccrash("No identifier with name ${cascadingNames[0]}")

    val (_, variable) = cascadingNames.drop(1).fold(firstName to first) { (prevName, variable), name ->
        if (variable !is TypeInstance) {
            crash("Trying to call $name on non-defined-type $prevName")
        }

        val next = variable.fields.firstOrNull { it.identifier == name }                or ccrash("No identifier with name $name")
        next.identifier to next
    }

    success(variable)
}
