@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsers

import org.toptobes.lang.nodes.*
import org.toptobes.lang.utils.MutVarDefs
import org.toptobes.lang.utils.VarDefs
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun variableDefinition(vars: MutVarDefs) = contextual { ctx ->
    val isConst = (ctx tryParse -str("const")) != null

    val isImmediate = (ctx tryParse -str("imm")) != null

    val typeName = ctx parse -identifier or cfail("Not a @declaration")

    val name = ctx parse -sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] } or ccrash("Error with @declaration name")

    val definition = ctx parse when (typeName) {
        "byte", "db" -> {
            byteVariableDefinition(name)
        }
        "word", "dw" -> {
            wordVariableDefinition(name)
        }
        in vars -> {
            typeVariableDefinition(name, typeName, vars)
        }
        else -> fail("Not a custom type @declaration")
    } or ccrash("Error with @$name declaration")

    definition.isConstant  = isImmediate || isConst
    definition.isImmediate = isImmediate

    vars += name to definition

    success(definition)
}

private fun byteVariableDefinition(name: String) = contextual { ctx ->
    ctx.tryParse(-equals) {
        val bytes = ctx parse bytes(name) or ccrash("Error with @byte definition")
        success(bytes)
    }

    ctx.tryParse(-word) { numBytes ->
        val zero = 0.toByte()
        val bytes = List(numBytes.toInt()) { zero }
        success(MultiByteVarDefinition(name, bytes))
    }

    crash("@byte definition doesn't have equals nor size")
}

private fun wordVariableDefinition(name: String) = contextual { ctx ->
    ctx.tryParse(-equals) {
        val words = ctx parse words(name) or ccrash("Error with @word definition")
        success(words)
    }

    ctx.tryParse(-word) { numWords ->
        val zero = 0.toShort()
        val words = List(numWords.toInt()) { zero }
        success(MultiWordVarDefinition(name, words))
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

private fun bytes(name: String) = any(
    byte.map { ByteVarDefinition(name, it) },
    cStyleArrayOf(byte).map { MultiByteVarDefinition(name, it) },
    string()..{ str -> str.map(Int::toByte) }..{ MultiByteVarDefinition(name, it) },
)

private fun words(name: String) = any(
    word.map { WordVarDefinition(name, it) },
    cStyleArrayOf(word).map { MultiWordVarDefinition(name, it) },
    string()..{ str -> str.map(Int::toShort) }..{ MultiWordVarDefinition(name, it) },
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

    val values = ctx parse parseConstructorArgs(type, vars)            or ccrash("'$typeName' error with parsing constructor args")

    ctx parse -str('}')                                                 or ccrash("'$typeName' constructor missing close }")

    success(values)
}

private fun parseConstructorArgs(type: DefinedType, vars: VarDefs) = contextual { ctx ->
    val values = mutableListOf<StaticDefinition>()
    val fields = type.declaredFields.toMutableList()

    val isNamedCheck = -!identifier then -str(":")
    val isNamedConstructor = ctx canParse isNamedCheck

    while (fields.isNotEmpty()) {
        val nextFieldType = if (isNamedConstructor) {
            ctx parse nextNamedTypeConstructorField(type, fields)       or ccrash("Error parsing (named) field in ${type.identifier} constructor")
        } else {
            nextUnnamedTypeConstructorField(fields)
        }

        val nextFieldName = nextFieldType.fieldName

        val testVariableNext = ctx tryParse variable(vars)
        val testAddressNext = ctx tryParse constAsAddress

        values += when (nextFieldType) {
            is TypeDefinitionFieldByte -> {
                if (testVariableNext is ByteVarDefinition) {
                    testVariableNext
                } else {
                    val byte = (ctx parse imm8(vars)  or ccrash("Byte field '$nextFieldName' not being assigned byte in ${type.identifier} constructor")).value
                    ByteVarDefinition(nextFieldName, byte)
                }
            }
            is TypeDefinitionFieldWord -> {
                if (testVariableNext is WordVarDefinition) {
                    testVariableNext
                } else if (testAddressNext != null) {
                    testAddressNext
                } else {
                    val word = (ctx parse imm16(vars) or ccrash("Word field '$nextFieldName' not being assigned word in ${type.identifier} constructor")).value
                    WordVarDefinition(nextFieldName, word)
                }
            }
            is TypeDefinitionFieldType -> {
                if (testVariableNext is TypeInstance) {
                    if (!testVariableNext.isConstant) {
                        crash("Trying to assign non-const ${testVariableNext.identifier} to field")
                    }
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

private fun nextUnnamedTypeConstructorField(fields: MutableList<TypeDefinitionField>) =
    fields.removeFirst()

private fun nextNamedTypeConstructorField(type: DefinedType, fields: MutableList<TypeDefinitionField>) = contextual { ctx ->
    val fieldNameParser = -any(*fields.map { str(it.fieldName) }.toTypedArray())
    val fieldName = ctx parse fieldNameParser or ccrash("Type constructor for '${type.identifier}' missing field(s)")
    val fieldTypeIdx = fields.indexOfFirst { it.fieldName == fieldName }

    val fieldType = fields[fieldTypeIdx]
    fields.removeAt(fieldTypeIdx)

    (ctx parse -str(":")) or ccrash("Named type constructor for '${type.identifier}' missing a :")
    success(fieldType)
}
