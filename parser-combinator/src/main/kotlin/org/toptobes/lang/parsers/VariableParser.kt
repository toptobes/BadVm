@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsers

import org.toptobes.byteVarUsage
import org.toptobes.lang.nodes.*
import org.toptobes.lang.utils.MutVarDefs
import org.toptobes.lang.utils.VarDefs
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*
import org.toptobes.wordVarUsage

fun variableDefinition(nodes: Identifiables) = contextual { ctx ->
    val isEmbedded  = ctx canTryParse -str("embed")
    val isImmediate = ctx canTryParse -str("imm")

    if (isEmbedded && isImmediate) {
        crash("Can't have a both embedded and immediate variable")
    }

    val typeName = ctx parse -identifier or cfail("Not a @declaration")

    val name = ctx parse -sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] } or ccrash("Error with @declaration name")

    val definition = ctx parse when (typeName) {
        "byte", "db" -> {
            byteVariableDefinition(name, nodes)
        }
        "word", "dw" -> {
            wordVariableDefinition(name, nodes)
        }
        in nodes.typeDefs -> {
            typeVariableDefinition(name, typeName, nodes)
        }
        else -> fail("Not a custom type @declaration")
    } or ccrash("Error with @$name declaration")

    definition.allocType = when {
        isEmbedded  -> Embedded
        isImmediate -> Immediate
        else -> Allocated
    }

    vars += name to definition

    success(definition)
}

private fun byteVariableDefinition(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(-equals) {
        val bytes = ctx parse bytes(name, nodes) or ccrash("Error with @byte definition")
        success(bytes)
    }

    ctx.tryParse(-word) { numBytes ->
        val initializer = ((ctx tryParse -word)?.toInt() ?: 0).toByte()
        val bytes = List(numBytes.toInt()) { initializer }
        success(ByteArrayInstance(name, bytes))
    }

    crash("@byte definition doesn't have equals nor size")
}

private fun wordVariableDefinition(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(-equals) {
        val words = ctx parse words(name, nodes) or ccrash("Error with @word definition")
        success(words)
    }

    ctx.tryParse(-word) { numWords ->
        val initializer = ((ctx tryParse -word)?.toInt() ?: 0).toShort()
        val words = List(numWords.toInt()) { initializer }
        success(WordArrayInstance(name, words))
    }

    crash("@word definition doesn't have equals nor size")
}

private fun typeVariableDefinition(name: String, typeName: String, vars: VarDefs) = contextual { ctx ->
    val type = vars[typeName]!!

    if (type !is DefinedType) {
        crash("Trying to instantiate an undefined type (only declared)")
    }

    ctx parse -equals or ccrash("@$name has no equals")

    ctx.tryParse(-str("?")) {
        success(ZeroedTypeInstance(name, type))
    }

    val fields = ctx parse typeConstructor(type.identifier, vars) or ccrash("Error parsing @$name's fields")
    success(TypeInstance(name, type, fields))
}

// -- PRIMITIVE CONSTRUCTORS --

private fun bytes(name: String, nodes: Identifiables) = any(
    byteVarUsage(nodes),
    byte.map { ByteInstance(name, it) },
    cStyleArrayOf(byte).map { ByteArrayInstance(name, it) },
    string()..{ str -> str.map(Int::toByte) }..{ ByteArrayInstance(name, it) },
)

private fun words(name: String, nodes: Identifiables) = any(
//    wordVarUsage(nodes),
    word.map { WordInstance(name, it) },
    cStyleArrayOf(word).map { WordArrayInstance(name, it) },
)

private fun <R> cStyleArrayOf(parser: Parser<String, R>): between<String, List<R>> {
    return between.curlyBrackets(sepBy.commas(parser))
}

private fun string() = between.doubleQuotes(until(char) { it.result == '"' })
    .map { it.map(Char::code) }

// -- TYPE CONSTRUCTORS --

private fun typeConstructor(typeName: String, vars: VarDefs): Parser<String, List<StaticDefinition>> = contextual { ctx ->
    val type = vars[typeName]                                          or ccrash("'$typeName' is an invalid type name")

    if (type !is DefinedType) {
        crash("Undefined type thunk ${type.identifier} trying to be constructed")
    }

    ctx parse str(type.identifier)                                      or ccrash("'$typeName' constructor missing valid preceding name")

    ctx parse -str('{')                                                 or ccrash("'$typeName' constructor missing open {")

    val values = ctx parse parseConstructorArgs(type, vars)             or ccrash("'$typeName' error with parsing constructor args")

    ctx parse -str('}')                                                 or ccrash("'$typeName' constructor missing close }")

    success(values)
}

private fun parseConstructorArgs(type: DefinedType, vars: VarDefs) = contextual { ctx ->
    val values = mutableListOf<StaticDefinition>()
    val fields = type.declaredFields.toMutableList()

    val isNamedCheck = -!identifier then -str(":")
    val isNamedConstructor = ctx canPeek isNamedCheck

    while (fields.isNotEmpty()) {
        val nextFieldType = if (isNamedConstructor) {
            ctx parse nextNamedTypeConstructorField(type, fields) or ccrash("Error parsing (named) field in ${type.identifier} constructor")
        } else {
            ctx parse nextUnnamedTypeConstructorField(fields)     or ccrash("Error parsing (unnamed) field in ${type.identifier} constructor")
        }

        val nextFieldName = nextFieldType.fieldName

        val testVariableNext = ctx tryParse variable(vars)

        values += when (nextFieldType) {
            is TypeDefinitionFieldByte -> {
                if (testVariableNext is ByteInstance) {
                    testVariableNext
                } else {
                    val byte = (ctx parse imm8(vars)  or ccrash("Byte field '$nextFieldName' not being assigned byte in ${type.identifier} constructor")).value
                    ByteInstance(nextFieldName, byte)
                }
            }
            is TypeDefinitionFieldWord -> {
                if (testVariableNext is WordInstance) {
                    testVariableNext
                } else {
                    val word = (ctx parse imm16(vars) or ccrash("Word field '$nextFieldName' not being assigned word in ${type.identifier} constructor")).value
                    WordInstance(nextFieldName, word)
                }
            }
            is TypeDefinitionFieldType -> {
                if (testVariableNext is TypeInstance) {
                    testVariableNext
                } else {
                    val constructor = typeConstructor(nextFieldType.typeFn().identifier, vars)

                    val nestedType =
                        (ctx.tryParse(str('?')) {
                            ZeroedTypeInstance(nextFieldName, type)
                        } ?:
                        ctx.tryParse(constructor) {
                            TypeInstance(nextFieldName, type, it)
                        }) or ccrash("Issue assigning type field '$nextFieldName'")

                    nestedType
                }
            }
        }

        if (fields.isNotEmpty()) {
            ctx parse -str(',') or ccrash("'${type.identifier}' constructor missing comma")
        }
    }

    ctx parse !-str(',')

    success(values)
}

private fun nextUnnamedTypeConstructorField(fields: MutableList<TypeDefinitionField>) = contextual { ctx ->
    val field = fields.removeFirst()
    val fieldName = field.fieldName

    (ctx tryParse -strOf(fieldName, -str(":")))
    success(field)
}

private fun nextNamedTypeConstructorField(type: DefinedType, fields: MutableList<TypeDefinitionField>) = contextual { ctx ->
    val fieldNameParser = -any(*fields.map { str(it.fieldName) }.toTypedArray())
    val fieldName = ctx parse fieldNameParser or ccrash("Type constructor for '${type.identifier}' missing field(s)")
    val fieldTypeIdx = fields.indexOfFirst { it.fieldName == fieldName }

    val fieldType = fields[fieldTypeIdx]
    fields.removeAt(fieldTypeIdx)

    (ctx parse -str(":")) or ccrash("Named type constructor for '${type.identifier}' missing a :")
    success(fieldType)
}
