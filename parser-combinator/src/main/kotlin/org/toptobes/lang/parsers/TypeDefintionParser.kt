@file:Suppress("MoveVariableDeclarationIntoWhen")

package org.toptobes.lang.parsers

import org.toptobes.lang.nodes.*
import org.toptobes.lang.utils.MutVarDefs
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun typeDefinition(vars: MutVarDefs) = any(definedType(vars), declaredType(vars))

fun declaredType(vars: MutVarDefs) = contextual { ctx ->
    ctx parse -str("declare")                                or cfail("Not a struct declaration")
    ctx parse -str("type")                                   or ccrash("No 'type' after 'declare'")

    val name = ctx parse identifier                          or ccrash("Error with struct name definition")
    vars += name to DeclaredType(name)

    success(NodeToDelete)
}

fun definedType(vars: MutVarDefs) = contextual { ctx ->
    ctx parse -str("type")                                  or cfail("Not a struct definition")

    val name = ctx parse identifier                         or ccrash("Error with struct name definition")

    vars += name to DeclaredType(name)

    ctx parse -equals                                       or ccrash("Struct $name missing equals")

    val fields = ctx parse fieldsParser(name, vars)        or ccrash("Issue parsing fields for $name")

    if (fields.isEmpty()) {
        crash("Struct $name has no fields")
    }

    if (fields.distinctBy { it.fieldName }.size != fields.size) {
        crash("Struct $name has duplicate field name(s)")
    }

    vars += name to DefinedType(name, fields)
    success(NodeToDelete)
}

private fun fieldsParser(typeName: String, vars: MutVarDefs) = contextual { ctx ->
    val fields = mutableListOf<TypeDefinitionField>()
    var hasMatched = false

    while (true) {
        val next = ctx peek -char                       or ccrash("EOF when parsing '$typeName' fields")

        when (next) {
            '&' -> fields += ctx parse sumTypeParser(vars) or ccrash("Error adding '$typeName' fields from sum type")
            '|' -> fields += ctx parse fieldParser(vars)   or ccrash("Error adding '$typeName' field")
            else -> break
        }
        hasMatched = true
    }

    if (hasMatched) {
        success(fields)
    } else {
        crash("Type has no fields")
    }
}

private fun fieldParser(vars: MutVarDefs) = contextual { ctx ->
    ctx parse -char

    val fieldConstructor =
        ctx.tryParse(-str("addr")) {
            val targetTypeName = ctx parse -identifier or ccrash("Addr @declaration missing identifier")

            if (targetTypeName !in vars) {
                crash("Addr @declaration $targetTypeName not defined")
            }

            { name: String -> TypeDefinitionFieldWord(name) }
        } ?:
        ctx.tryParse(-str("byte")) {
            ::TypeDefinitionFieldByte
        } ?:
        ctx.tryParse(-str("word")) {
            ::TypeDefinitionFieldWord
        } ?:
        (ctx parse -typeFieldType(vars))!!
            .let { typeFn ->
                { name: String -> TypeDefinitionFieldType(name, typeFn) }
            }

    val name = (ctx parse -identifier) ?: crash("Error with custom type field name")

    success(fieldConstructor(name))
}

private fun typeFieldType(vars: MutVarDefs) = contextual { ctx ->
    val typeName = (ctx parse -identifier)

    val type = vars[typeName]
        ?: crash("$typeName is not a type (field)")

    if (type !is TypeDefinition) {
        crash("Tried to assign ${type.javaClass.name} to a type field")
    }

    success { vars[typeName]!! as TypeDefinition }
}

private fun sumTypeParser(vars: MutVarDefs) = contextual { ctx ->
    ctx parse -char

    val typeFn = ctx parse typeFieldType(vars) or ccrash("Sum type has invalid type")
    val type = typeFn()

    if (type !is DefinedType) {
        crash("Undefined type thunk ${type.identifier} trying to be summed")
    }

    success(type.declaredFields)
}
