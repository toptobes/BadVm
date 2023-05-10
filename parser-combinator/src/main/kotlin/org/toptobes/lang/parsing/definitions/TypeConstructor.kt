package org.toptobes.lang.parsing.definitions

import org.toptobes.lang.ast.*
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

private fun parseConstructorArgs(type: TypeDefinition, name: String, allocType: AllocationType) = contextual {
    type.ensureIsConcrete()

    val values = mutableListOf<VarDefinition>()
    val fields = type.fields.toMutableList()

    val isNamedCheck = -!identifier then -str(":")
    val isNamedConstructor = ctx canPeek isNamedCheck

    while (fields.isNotEmpty()) {
        val nextField = if (isNamedConstructor) {
            ctx parse nextNamedTypeConstructorField(type, fields) orCrash "Error parsing (named) field in ${type.name} constructor"
        } else {
            ctx parse nextUnnamedTypeConstructorField(fields) orCrash "Error parsing (unnamed) field in ${type.name} constructor"
        }

        values += when (nextField) {
            is ByteField -> byteField(nextField, name, allocType)
            is WordField -> wordField(nextField, name, allocType)
            is NestedTypeField -> nestedTypeField(nextField, name, allocType)
        }

        if (fields.isNotEmpty()) {
            ctx parse -str(',') orCrash "'${type.name}' constructor missing comma"
        }
    }

    ctx parse !-str(',')

    succeed(values)
}

private fun ContextScope<*>.byteField(field: ByteField, name: String, allocType: AllocationType): List<VarDefinition> {
    return ctx parse -byteConstructor(name + field.name, allocType) orCrash "Error parsing byte constructor"
}

private fun ContextScope<*>.wordField(field: WordField, name: String, allocType: AllocationType): List<VarDefinition> {
    return ctx parse -wordConstructor(name + field.name, allocType) orCrash "Error parsing word constructor"
}

private fun ContextScope<*>.nestedTypeField(field: NestedTypeField, name: String, allocType: AllocationType): List<VarDefinition> {
    ctx parse -str(field.type) orCrash "Error parsing constructor name for ${field.type}"
    return ctx parse -typeConstructor(field.type, name + field.name, allocType) orCrash "Error parsing constructor for ${field.type}"
}

private fun nextUnnamedTypeConstructorField(fields: MutableList<Field>) = contextual { ctx ->
    val field = fields.removeFirst()
    val fieldName = field.name

    (ctx parse -strOf(fieldName, -str(":")))
    succeed(field)
}

private fun nextNamedTypeConstructorField(type: TypeDefinition, fields: MutableList<Field>) = contextual { ctx ->
    val fieldNameParser = -any(*fields.map { str(it.name) }.toTypedArray())
    val fieldName = ctx parse fieldNameParser orCrash "Type constructor for '${type.name}' missing field(s)"
    val fieldTypeIdx = fields.indexOfFirst { it.name == fieldName }

    val fieldType = fields[fieldTypeIdx]
    fields.removeAt(fieldTypeIdx)

    (ctx parse -str(":")) orCrash "Named type constructor for '${type.name}' missing a :"
    succeed(fieldType)
}
