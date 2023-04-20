@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION", "MoveVariableDeclarationIntoWhen")

package org.toptobes.lang.parsers

import org.toptobes.lang.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun definitionParser(types: MutableMap<String, Type>) = any(
    labelDefinition,
    variableWordDef,
    variableByteDef,
    variableTypeDef(types),
    constWordDef,
    constByteDef,
    type(types),
)

// -- UTIL --

private val byteType = any(str("db"), str("byte"))
private val wordType = any(str("dw"), str("word"))

private fun <R> cStyleArrayOf(parser: Parser<String, R>): between<String, List<R>> {
    return between.curlyBrackets(sepBy.whitespaceInsensitiveCommas(parser))
}

private val string = between.doubleQuotes(until(char) { it.result == '"' })
    .map { it.map(Char::code) }

private val equals = str("=")

private val singleByteList = byte.map { listOf(it) }
private val multiByteList  = cStyleArrayOf(byte)
private val byteString = string..{ str -> str.map(Int::toByte) }

private val singleWordList = word.map { listOf(it) }
private val multiWordList  = cStyleArrayOf(word)
private val wordString = string..{ str -> str.map(Int::toShort) }

// -- VARIABLE DEFINITIONS --

val variableByteDef = contextual { ctx ->
    (ctx parse -byteType)                                                      ?: fail("Not a @byte declaration")

    val name = (ctx parse sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] })                                                              ?: crash("Error with @byte name")

    ctx.ifParseable(-equals) {
        val bytes = (ctx parse any(singleByteList, multiByteList, byteString)) ?: crash("Error with @byte definition")
        success(Var8Definition(name, bytes))
    }

    ctx.ifParseable(-word) { numBytes ->
        val zero = 0.toByte()
        val bytes = List(numBytes.toInt()) { zero }
        success(Var8Definition(name, bytes))
    }

    crash("Variable byte definition doesn't have -equals nor size")
}

val variableWordDef = contextual { ctx ->
    (ctx parse -wordType)                                                      ?: fail("Not a @word declaration")

    val name = (ctx parse sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] })                                                              ?: crash("Error with @word name")

    ctx.ifParseable(-equals) {
        val words = (ctx parse any(singleWordList, multiWordList, wordString)) ?: crash("Error with @word definition")
        success(Var16Definition(name, words))
    }

    ctx.ifParseable(-word) { numWords ->
        val zero = 0.toShort()
        val words = List(numWords.toInt()) { zero }
        success(Var16Definition(name, words))
    }

    crash("Variable word definition doesn't have -equals nor size")
}

// -- CONST DEFINITIONS --

val constByteDef = contextual { ctx ->
    (ctx parse (str("const") then -byteType))       ?: fail("Not a \$byte declaration")
    (ctx parse !str("$"))

    val name = (ctx parse identifier)               ?: crash("Error with \$byte name declaration")

    (ctx parse -equals)                              ?: crash("\$byte definition doesn't have an -equals")

    val byte = (ctx parse byte)                     ?: crash("\$byte is not assigned to a proper byte")

    success(Const8Definition(name, byte))
}

val constWordDef = contextual { ctx ->
    (ctx parse (str("const") then -wordType))       ?: fail("Not a \$word declaration")
    (ctx parse !str("$"))

    val name = (ctx parse identifier)               ?: crash("Error with \$word name declaration")

    (ctx parse -equals)                              ?: crash("\$word definition doesn't have an -equals")

    val word = (ctx parse word)                     ?: crash("\$word is not assigned to a proper word")

    success(Const16Definition(name, word))
}

// -- LABEL DEFINITION --

val labelDefinition = strOf(identifier, str(':'))
    .map { it.dropLast(1) }
    .map(::LabelDefinition)

// -- STRUCTS ==

private fun typeFieldType(types: MutableMap<String, Type>) = contextual { ctx ->
    val typeName = (ctx parse -identifier)

    val type = types[typeName]
        ?: crash("$typeName is not a type (field)")

    success(type)
}

private fun sumTypeParser(types: MutableMap<String, Type>) = contextual { ctx ->
    ctx parse -char

    val type = ctx parse typeFieldType(types) or ccrash("Sum type has invalid type")

    if (type !is DefinedType) {
        crash("Undefined type thunk ${type.identifier} trying to be summed")
    }

    success(type.declaredFields)
}

private fun fieldParser(types: MutableMap<String, Type>) = contextual { ctx ->
    ctx parse -char

    val fieldConstructor =
        ctx.ifParseable(-byteType) {
            ::DeclaredTypeFieldByte
        } ?:
        ctx.ifParseable(-wordType) {
            ::DeclaredTypeFieldWord
        } ?:
        (ctx parse -typeFieldType(types))!!
            .let { type ->
                { name: String ->
                    DeclaredTypeFieldType(name, type)
                }
            }

    val name = (ctx parse -identifier)          ?: crash("Error with custom type field name")

    success(fieldConstructor(name))
}

private fun fieldsParser(typeName: String, types: MutableMap<String, Type>) = contextual { ctx ->
    val fields = mutableListOf<DeclaredTypeField>()
    var hasMatched = false

    while (true) {
        val next = ctx tryParse -char                       or ccrash("EOF when parsing '$typeName' fields")

        when (next) {
            '&' -> fields += ctx parse sumTypeParser(types) or ccrash("Error adding '$typeName' fields from sum type")
            '|' -> fields += ctx parse fieldParser(types)   or ccrash("Error adding '$typeName' field")
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

fun definedType(types: MutableMap<String, Type>) = contextual { ctx ->
    ctx parse -str("type")                                  or cfail("Not a struct definition")

    val name = ctx parse identifier                         or ccrash("Error with struct name definition")

    types += name to DeclaredType(name)

    ctx parse -equals                                       or ccrash("Struct $name missing equals")

    val fields = ctx parse fieldsParser(name, types)        or ccrash("Issue parsing fields for $name")

    if (fields.isEmpty()) {
        crash("Struct $name has no fields")
    }

    if (fields.distinctBy { it.fieldName }.size != fields.size) {
        crash("Struct $name has duplicate field name(s)")
    }

    types += name to DefinedType(name, fields)
    success(DeleteThisNode)
}

fun declaredType(types: MutableMap<String, Type>) = contextual { ctx ->
    ctx parse -str("declare")                                or cfail("Not a struct declaration")
    ctx parse -str("type")                                   or ccrash("No 'type' after 'declare'")

    val name = ctx parse identifier                          or ccrash("Error with struct name definition")
    types += name to DeclaredType(name)

    success(DeleteThisNode)
}

fun type(types: MutableMap<String, Type>) = any(definedType(types), declaredType(types))

// -- CUSTOM TYPE VAR --

fun nextUnnamedTypeConstructorField(fields: MutableList<DeclaredTypeField>) = contextual<String, DeclaredTypeField> { ctx ->
    val nextField = fields.removeFirst()
    success(nextField)
}

fun nextNamedTypeConstructorField(type: DefinedType, fields: MutableList<DeclaredTypeField>) = contextual { ctx ->
    val fieldNameParser = -any(*fields.map { str(it.fieldName) }.toTypedArray())
    val fieldName = ctx parse fieldNameParser or ccrash("Type constructor for '${type.identifier}' missing field(s)")
    val fieldTypeIdx = fields.indexOfFirst { it.fieldName == fieldName }

    val fieldType = fields[fieldTypeIdx]
    fields.removeAt(fieldTypeIdx)

    (ctx parse -equals) or ccrash("Named type constructor for '${type.identifier}' missing an =")
    success(fieldType)
}

fun parseConstructorArgs(type: DefinedType, types: MutableMap<String, Type>) = contextual { ctx ->
    val values = mutableListOf<TypeField>()
    val fields = type.declaredFields.toMutableList()

    val isNamedCheck = -identifier then -str("=")
    val isNamedConstructor = ctx canParse isNamedCheck

    while (fields.isNotEmpty()) {
        val nextFieldType = (ctx parse if (isNamedConstructor) {
            nextNamedTypeConstructorField(type, fields)
        } else {
            nextUnnamedTypeConstructorField(fields)
        })!!

        val nextFieldName = nextFieldType.fieldName

        values += when (nextFieldType) {
            is DeclaredTypeFieldByte -> {
                val byte = (ctx parse imm8  or ccrash("Byte field '$nextFieldName' not being assigned byte")).value
                TypeFieldByte(nextFieldName, byte)
            }
            is DeclaredTypeFieldWord -> {
                val word = (ctx parse imm16 or ccrash("Word field '$nextFieldName' not being assigned word")).value
                TypeFieldWord(nextFieldName, word)
            }
            is DeclaredTypeFieldType -> {
                val constructor = typeConstructor(nextFieldType.type.identifier, types)

                val nestedType =
                    (ctx.ifParseable(str('?')) {
                        ZeroedTypeInstance("", type)
                    } ?:
                    ctx.ifParseable(constructor) {
                        DefinedTypeInstance("", type, it)
                    })                                                  or ccrash("Issue assigning type field '$nextFieldName'")

                TypeFieldType(nextFieldName, nestedType)
            }
        }

        if (fields.isNotEmpty()) {
            ctx parse -str(',')                                         or ccrash("'${type.identifier}' constructor missing comma")
        }
    }

    ctx parse !-str(',')

    success(values)
}

fun typeConstructor(typeName: String, types: MutableMap<String, Type>): Parser<String, List<TypeField>> = contextual { ctx ->
    val type = types[typeName]                                          or ccrash("'$typeName' is an invalid type name")

    if (type !is DefinedType) {
        crash("Undefined type thunk ${type.identifier} trying to be constructed")
    }

    ctx parse str(type.identifier)                                      or ccrash("'$typeName' constructor missing preceding name")

    ctx parse -str('{')                                                 or ccrash("'$typeName' constructor missing open {")

    val values = ctx parse parseConstructorArgs(type, types)            or ccrash("'$typeName' error with parsing constructor args")

    ctx parse -str('}')                                                 or ccrash("'$typeName' constructor missing close }")

    success(values)
}

fun variableTypeDef(types: MutableMap<String, Type>) = contextual { ctx ->
    val typeName = (ctx parse -identifier)
    val type = types[typeName]                                          or cfail("Not a custom var declaration")

    val name = ctx parse sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] }                                                        or ccrash("Error with custom var declaration")

    ctx parse -equals                                                   or ccrash("No = for custom var declaration")

    ctx.ifParseable(-str("?")) {
        success(ZeroedTypeInstance(name, type))
    }

    val fields = ctx parse typeConstructor(type.identifier, types)      or ccrash("Error parsing $name's fields")
    success(DefinedTypeInstance(name, type, fields))
}
