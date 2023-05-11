package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.*
import org.toptobes.lang.parsing.AllocationType
import org.toptobes.lang.parsing.identifier
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.impls.strOf

fun typeConstructor(name: String, allocType: AllocationType) = contextual {
    val typeName = ctx parse identifier orFail "Not a type constructor"
    val vars = ctx parse typeConstructor(typeName, name, allocType) orCrash "Error parsing type constructor"
    succeed(vars)
}

private fun typeConstructor(typeName: String, name: String, allocType: AllocationType) = contextual {
    ctx parse -str("{") orFail "Not a type constructor"

    val type = ctx.state.types[typeName] ?: fail("$typeName is not a type")
    type.ensureIsConcrete()

    val vars = ctx parse parseConstructorArgs(type, "$name.", allocType) orCrash "Error parsing constructor args"

    ctx parse -str("}") orCrash "Missing closing }"

    succeed(vars)
}

private fun parseConstructorArgs(type: TypeInterpretation, name: String, allocType: AllocationType) = contextual {
    type.ensureIsConcrete()

    val values = mutableListOf<Definition<*>>()
    val fields = type.fields.values.toMutableList()

    val isNamedCheck = -!identifier then -str(":")
    val isNamedConstructor = ctx canPeek isNamedCheck

    while (fields.isNotEmpty()) {
        val nextField = if (isNamedConstructor) {
            ctx parse nextNamedTypeConstructorField(type, fields) orCrash "Error parsing (named) field in ${type.typeName} constructor"
        } else {
            ctx parse nextUnnamedTypeConstructorField(fields) orCrash "Error parsing (unnamed) field in ${type.typeName} constructor"
        }

        values += when (nextField.interpretation) {
            is ByteInterpretation -> byteField(nextField, name, allocType)
            is WordInterpretation -> wordField(nextField, name, allocType)
            is TypeInterpretation -> nestedTypeField(nextField, name, allocType)
            else -> TODO()
        }

        if (fields.isNotEmpty()) {
            ctx parse -str(',') orCrash "'${type.typeName}' constructor missing comma"
        }
    }

    ctx parse !-str(',')

    succeed(values)
}

private fun ContextScope<*>.byteField(field: Field, name: String, allocType: AllocationType): List<Byte> {
    return ctx parse -byteConstructor(name + field.name, allocType) orCrash "Error parsing byte constructor"
}

private fun ContextScope<*>.wordField(field: Field, name: String, allocType: AllocationType): List<Byte> {
    return ctx parse -wordConstructor(name + field.name, allocType) orCrash "Error parsing word constructor"
}

private fun ContextScope<*>.nestedTypeField(field: Field, name: String, allocType: AllocationType): List<Byte> {
    ctx parse -str(field.type) orCrash "Error parsing constructor name for ${field.type}"
    return ctx parse -typeConstructor(field.type, name + field.name, allocType) orCrash "Error parsing constructor for ${field.type}"
}

private fun nextUnnamedTypeConstructorField(fields: MutableList<Field>) = contextual { ctx ->
    val field = fields.removeFirst()
    val fieldName = field.name

    (ctx parse -strOf(fieldName, -str(":")))
    succeed(field)
}

private fun nextNamedTypeConstructorField(type: TypeInterpretation, fields: MutableList<Field>) = contextual { ctx ->
    val fieldNameParser = -any(*fields.map { str(it.name) }.toTypedArray())
    val fieldName = ctx parse fieldNameParser orCrash "Type constructor for '${type.typeName}' missing field(s)"
    val fieldTypeIdx = fields.indexOfFirst { it.name == fieldName }

    val fieldType = fields[fieldTypeIdx]
    fields.removeAt(fieldTypeIdx)

    (ctx parse -str(":")) orCrash "Named type constructor for '${type.typeName}' missing a :"
    succeed(fieldType)
}
