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

    ctx.addType(TypeIntrp(name, false, emptyMap()))
    succeed(DeleteThisNode)
}

private val definedType = contextual {
    val isExport = ctx canParse -str("export")

    ctx parse -str("type") orFail "Not a type definition"

    val name = ctx parse -identifier orCrash "Error parsing declared type's identifier"

    ctx.addType(TypeIntrp(name, isExport, emptyMap()))

    ctx parse -str("=")

    val fields = ctx parse fieldsParser orCrash "Error parsing declared type's fields"

    ctx.addType(TypeIntrp(name, isExport, fields.associateBy { it.name }))
    succeed(DeleteThisNode)
}

private val fieldsParser = contextual {
    val fields = mutableListOf<Field<*>>()
    var currentOffset = 0

    while (true) {
        fields += when {
            ctx canParse str("|") -> {
                ctx parse normalField(currentOffset)   orCrash "Error parsing normal type field"
            }
            ctx canParse str("&") || currentOffset == 0 -> {
                ctx parse embeddedField(currentOffset) orCrash "Error parsing embedded type field"
            }
            else -> break
        }
        currentOffset += fields.last().size
    }

    succeed(fields)
}

private fun embeddedField(offset: Int) = contextual {
    val typeName = ctx parse -identifier orCrash "Error parsing type for field"

    if (typeName in listOf("addr", "word", "dw")) {
        crash("Can't embed a 'word' field")
    }

    if (typeName in listOf("byte", "db")) {
        crash("Can't embed a 'byte' field")
    }

    val type = ctx.lookup<TypeIntrp>(typeName) ?: crash("$typeName is not a type")
    type.ensureIsConcrete()

    succeed(type.adjustOffsets(offset).fields.values)
}

private fun normalField(offset: Int) = contextual {
    val typeName = ctx parse -identifier orCrash "Error parsing type for field"
    val name = ctx parse -identifier orCrash "Error parsing name for field"

    val field = when (typeName) {
        "addr", "byte", "db" -> Field(name, ByteIntrp, offset)
        "word", "dw" -> Field(name, WordIntrp, offset)
        else -> {
            val type = ctx.lookup<TypeIntrp>(typeName) ?: crash("$typeName is not a type")
            type.ensureIsConcrete()
            Field(name, type.adjustOffsets(offset), offset)
        }
    }

    succeed(listOf(field))
}

private fun TypeIntrp.adjustOffsets(offset: Int) = copy(fields = fields.map { (name, field) ->
    name to field.copy(offset = (field.offset + offset))
}.toMap())
