package org.toptobes

import org.toptobes.lang.nodes.*
import org.toptobes.lang.parsers.identifier
import org.toptobes.lang.toBytes
import org.toptobes.lang.utils.StatefulParsingException
import org.toptobes.lang.utils.StatelessParsingException
import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.sepBy
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.unaryMinus

fun main() {
    val H = DefinedType("H", listOf(TypeDefinitionFieldByte("s")))
    val h = TypeInstance("h", H, listOf(ByteVarDefinition("s", 1)))

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

    if (variable !is StaticDefinition) {
        throw StatelessParsingException("$variable is not a static definition")
    }

    when (variable) {
        is ByteVarDefinition -> when {
            isDeref -> throw StatelessParsingException("Can not deref variable $variable")
            else -> ByteVariable(variable.identifier, variable.byte)
        }
        is TypeInstance -> when {
            variable.isImmediate -> when (variable.size) {
                1 -> ByteVariable(variable.identifier, variable.toBytes()[0])
                2 -> WordVariable(variable.identifier, variable.toBytes().toWord())
                else -> EmbeddedBytesVariable(variable.identifier, variable.toBytes())
            }
            else -> throw StatelessParsingException("Can not directly use a non-imm type instance")
        }
        is WordVarDefinition -> when {
            isDeref -> AddrVariable(variable.identifier)
            else -> WordVariable(variable.identifier, variable.word)
        }
    }

    success(variable)
}

fun List<Byte>.toWord(): Word {
    return ((this[0].toInt() shl 8) + this[1]).toShort()
}
