@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsing.constructors

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.embeddedBytes
import org.toptobes.lang.parsing.identifier
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.betweenCurlyBrackets
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.impls.strOf

fun typeConstructor(name: String, typeName: String) = contextual {
    val type = ctx.lookup<TypeIntrp>(typeName) ?: fail()
    type.ensureIsConcrete()

    ctx.parse(embeddedBytes(type.size..type.size)) {
        succeed(it to type)
    }

    ctx parse -str(typeName) orCrash "Error parsing constructor name for $typeName"

    val bytes = ctx parse betweenCurlyBrackets(
        parseConstructorArgs(type, "$name.")
    ) orCrash "Error parsing constructor args"

    succeed(bytes to type)
}

@Suppress("UNCHECKED_CAST")
private fun parseConstructorArgs(type: TypeIntrp, name: String) = contextual {
    type.ensureIsConcrete()

    var values = byteArrayOf()
    val fields = type.fields.values.toMutableList()

    val isNamedCheck = -!identifier then -str(":")
    val isNamedConstructor = ctx canPeek isNamedCheck

    while (fields.isNotEmpty()) {
        val bytes = ctx parse embeddedBytes()

        if (bytes != null) {
            var size = 0

            while (size < bytes.size) {
                size += fields.removeFirstOrNull()?.size ?: crash("Too many embedded bytes (${bytes.size})")
            }

            if (size != bytes.size) {
                crash("Embedded bytes (sized ${bytes.size}) not aligned with fields")
            }

            values += bytes
        } else {
            val nextField = if (isNamedConstructor) {
                ctx parse nextNamedTypeConstructorField(type, fields) orCrash "Error parsing (named) field in ${type.name} constructor"
            } else {
                ctx parse nextUnnamedTypeConstructorField(fields) orCrash "Error parsing (unnamed) field in ${type.name} constructor"
            }

            values += when (nextField.intrp) {
                is ByteIntrp -> byteField()
                is WordIntrp -> wordField()
                is TypeIntrp -> nestedTypeField(nextField as Field<TypeIntrp>, name)
                else -> TODO()
            }
        }

        if (fields.isNotEmpty()) {
            ctx parse -str(',') orCrash "'${type.name}' constructor missing comma"
        }
    }

    ctx parse !-str(',')

    succeed(values)
}

private fun ContextScope<*>.byteField(): ByteArray {
    return ctx parse -singleByte orCrash "Error parsing byte constructor"
}

private fun ContextScope<*>.wordField(): ByteArray {
    return ctx parse -singleWord orCrash "Error parsing word constructor"
}

private fun ContextScope<*>.nestedTypeField(field: Field<TypeIntrp>, name: String): ByteArray {
    val typeName = field.intrp.name
    val type = ctx parse -typeConstructor(name + field.name, typeName) orCrash "Error parsing constructor for $typeName"
    return type.first
}

private fun nextUnnamedTypeConstructorField(fields: MutableList<Field<*>>) = contextual { ctx ->
    val field = fields.removeFirst()
    val fieldName = field.name

    ctx parse -strOf(fieldName, ":")
    succeed(field)
}

private fun nextNamedTypeConstructorField(type: TypeIntrp, fields: MutableList<Field<*>>) = contextual { ctx ->
    val fieldNameParser = -any(fields.map { str(it.name) })
    val fieldName = ctx parse fieldNameParser orCrash "Type constructor for '${type.name}' missing field(s)"
    val fieldTypeIdx = fields.indexOfFirst { it.name == fieldName }

    val fieldType = fields[fieldTypeIdx]
    fields.removeAt(fieldTypeIdx)

    ctx parse -str(":") orCrash "Named type constructor for '${type.name}' missing a :"
    succeed(fieldType)
}
