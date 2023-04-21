package org.toptobes

import org.toptobes.lang.nodes.*
import org.toptobes.lang.parsers.identifier
import org.toptobes.lang.utils.StatefulParsingException
import org.toptobes.lang.utils.VarDefs
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.sepBy
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.impls.strOf
import org.toptobes.parsercombinator.or
import org.toptobes.parsercombinator.withDefault

fun main() {
    """ 
        # 00000001
        type A = | word b
        A a = A{1}
        
        a.b    # invalid
        
        @a.b   # valid
        mov reg16, [a + offset(b)]
    """.trimIndent()

    """ 
        # 00000002, 00000001
        type B = | word c
        type A = | word _ | B b
        A a = A{1, B{2}}
        
        @a.b.c
        mov reg16, [a + offset(b) + offset(c)]
    """.trimIndent()

    """ 
        type B = | word c
        type A = | B b
        A a = A{B{0}}
        
        &a
        mov reg16, a
    """.trimIndent()

    """ 
        
    """.trimIndent()

    val H = DefinedType("H", listOf(TypeDefinitionFieldByte("s")))
    val vars = mutableMapOf(
        "H" to H,
        "h" to TypeInstance("H", H, listOf(ByteVarDefinition("s", 1)))
    )

    try {
        println(variable(vars)("@h.s"))
    } catch (e: StatefulParsingException) {
        println(e.message!!)
    }
}

fun variable(vars: VarDefs) = contextual { ctx ->
    val varIdentifier = strOf((str('@') or str('&') withDefault ""), identifier)

    val cascadingNames = ctx parse sepBy.periods(varIdentifier, allowTrailingSep = false) or ccrash("No identifier found for variable usage")
    val firstName = cascadingNames[0]
    val first = vars[firstName]                                                           or ccrash("No identifier with name ${cascadingNames[0]}")

    val (_, variable) = cascadingNames.drop(1).fold(firstName to first) { (prevName, variable), name ->
        if (variable !is TypeInstance) {
            crash("Trying to call $name on non-defined-type $prevName")
        }

        val next = variable.fields.firstOrNull { it.identifier == name }                  or ccrash("No identifier with name $name")
        next.identifier to next
    }

    success(variable)
}
