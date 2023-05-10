package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.impls.lazy
import org.toptobes.parsercombinator.unaryMinus

val typeDefinition = lazy { any(declaredType, definedType) }

private val declaredType = contextual {
    ctx parse -str("declare") orFail "Not a type declaration"
    ctx parse -str("type") orCrash "declare keyword without 'type' after it"

    val name = ctx parse -identifier orCrash "Error parsing declared type's identifier"

    ctx addType DeclaredTypeDefinition(name)
    succeed(DeleteThisNode)
}

private val definedType = contextual {
    ctx parse -str("type") orFail  "Not a type definition"

    val name = ctx parse -identifier orCrash "Error parsing declared type's identifier"

    ctx addType DeclaredTypeDefinition(name)

    ctx parse -str("=")

    val fields = ctx parse fieldsParser orCrash "Error parsing declared type's fields"

    ctx addType ConcreteTypeDefinition(name, fields)
    succeed(DeleteThisNode)
}

private val fieldsParser = contextual {
    val fields = mutableListOf<Field>()
    var firstMatch = true

    while (true) {
        fields += when {
            ctx canParse str("|") -> {
                ctx parse normalField   orCrash "Error parsing normal type field"
            }
            ctx canParse str("&") || firstMatch -> {
                ctx parse embeddedField orCrash "Error parsing embedded type field"
            }
            else -> break
        }
        firstMatch = false
    }

    succeed(fields)
}

private val embeddedField = contextual {
    val typeName = ctx parse -identifier orCrash "Error parsing type for field"

    if (typeName in listOf("word", "dw")) {
        crash("Can't embed a 'word' field")
    }

    if (typeName in listOf("byte", "db")) {
        crash("Can't embed a 'byte' field")
    }

    val type = ctx.state.types[typeName] ?: crash("$typeName is not a tyoe")
    type.ensureIsConcrete()

    succeed(type.fields)
}

private val normalField = contextual {
    val type = ctx parse -identifier orCrash "Error parsing type for field"
    val name = ctx parse -identifier orCrash "Error parsing name for field"

    val field = when (type) {
        "byte", "db" -> ByteField(name)
        "word", "dw" -> WordField(name)
        else -> NestedTypeField(name, type)
    }

    succeed(listOf(field))
}
