@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsers

import org.toptobes.lang.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun definitionParser(types: MutableSet<Type>) = any(
    labelDefinition,
    variableWordDef,
    variableByteDef,
//    customVarDefs(types),
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

private fun <R> semicolonSepArrOf(parser: Parser<String, R>): between<String, List<R>> {
    return between.curlyBrackets(sepBy(parser, -str(";")))
}

private val string = between.doubleQuotes(until(char) { it.result == '"' })
    .map { it.map(Char::code) }

private val equals = -str("=")

private val singleByteList = byte.map { listOf(it) }
private val multiByteList  = cStyleArrayOf(byte)
private val byteString = string..{ str -> str.map(Int::toByte) }

private val singleWordList = word.map { listOf(it) }
private val multiWordList  = cStyleArrayOf(word)
private val wordString = string..{ str -> str.map(Int::toShort) }

// -- VARIABLE DEFINITIONS --

val variableByteDef = contextual { ctx ->
    (ctx parse -byteType) ?: fail("Not a var byte declaration")

    val name = (ctx parse sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] }) ?: crash("Error with var byte declaration")

    ctx.ifParseable(equals) {
        val bytes = (ctx parse any(singleByteList, multiByteList, byteString)) ?: crash("Error with = var byte definition")
        success(Var8Definition(name, bytes))
    }

    ctx.ifParseable(-word) { numBytes ->
        val zero = 0.toByte()
        val bytes = List(numBytes.toInt()) { zero }
        success(Var8Definition(name, bytes))
    }

    crash("Var byte definition doesn't have equals nor size")
}

val variableWordDef = contextual { ctx ->
    (ctx parse -wordType) ?: fail("Not a var word declaration")

    val name = (ctx parse sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] }) ?: crash("Error with var word declaration")

    ctx.ifParseable(equals) {
        val words = (ctx parse any(singleWordList, multiWordList, wordString)) ?: crash("Error with = var word definition")
        success(Var16Definition(name, words))
    }

    ctx.ifParseable(-word) { numWords ->
        val zero = 0.toShort()
        val words = List(numWords.toInt()) { zero }
        success(Var16Definition(name, words))
    }

    crash("Var word definition doesn't have equals nor size")
}

// -- CONST DEFINITIONS --

val constByteDef = contextual { ctx ->
    (ctx parse (str("const") then -byteType)) ?: fail("Not a const byte declaration")
    (ctx parse !str("$"))

    val name = (ctx parse identifier) ?: crash("Error with const byte name declaration")

    (ctx parse equals) ?: crash("Const byte definition doesn't have an equals")

    val byte = (ctx parse byte) ?: crash("Const byte is not assigned to a proper byte")

    success(Const8Definition(name, byte))
}

val constWordDef = contextual { ctx ->
    (ctx parse (str("const") then -wordType)) ?: fail("Not a const word declaration")
    (ctx parse !str("$"))

    val name = (ctx parse identifier) ?: crash("Error with const word name declaration")

    (ctx parse equals) ?: crash("Const word definition doesn't have an equals")

    val word = (ctx parse word) ?: crash("Const word is not assigned to a proper word")

    success(Const16Definition(name, word))
}

// -- LABEL DEFINITION --

val labelDefinition = strOf(identifier, str(':'))
    .map { it.dropLast(1) }
    .map(::LabelDefinition)

// -- STRUCTS ==

private val typeField8 = contextual { ctx ->
    (ctx parse -byteType) ?: fail("Not a byte type field")

    val name = (ctx parse identifier) ?: crash("Error with byte type field name")

    (ctx parse -str(';')) ?: crash("Struct field $name missing semicolon")

    success(TypeField8(name))
}

private val typeField16 = contextual { ctx ->
    (ctx parse -wordType) ?: fail("Not a word type field")

    val name = (ctx parse identifier) ?: crash("Error with word type field name")

    (ctx parse -str(';')) ?: crash("Struct field $name missing semicolon")

    success(TypeField16(name))
}

private fun customTypeField(types: MutableSet<Type>) = contextual { ctx ->
    val typeName = (ctx parse -identifier)

    val type = types.find { it.identifier == typeName }
        ?: fail("$typeName is not a type (field)")
    
    val name = (ctx parse identifier) ?: crash("Error with custom type field name")

    (ctx parse -str(';')) ?: crash("Struct field $name missing semicolon")

    success(TypeFieldCustom(name, type))
}

fun type(types: MutableSet<Type>) = contextual { ctx ->
    (ctx parse -str("type")) ?: fail("Not a struct declaration")

    val name = (ctx parse identifier) ?: crash("Error with struct name declaration")

    types += TypeThunk(name)
    
    (ctx parse equals) ?: crash("Struct $name missing equals")

    (ctx parse -str('{')) ?: crash("Struct $name missing opening {")

    val fields = (ctx parse +-any(typeField8, typeField16, customTypeField(types)))
        ?: crash("Error with type fields")

    (ctx parse -str('}')) ?: crash("Struct $name missing closing }")

    if (fields.isEmpty()) {
        crash("Struct $name has no fields")
    }

    if (fields.map { it.identifier }.toSet().size != fields.size) {
        crash("Struct $name has duplicate field name(s)")
    }

    types += TypeImpl(name, fields)
    success(TypeDefinition)
}

// -- CUSTOM TYPE VAR --

fun typeDeclaration(types: Set<Type>): Parser<String, *> = contextual { ctx ->
    val typeName = (ctx parse -identifier)

    val type = types.find { it.identifier == typeName }
        ?: fail("$typeName is not a type")

    (ctx parse str('(')) ?: crash("$typeName constructor missing open (")

    if (type !is TypeImpl) crash("Undefined type thunk ${type.identifier} trying to be constructed")

    val fields = type.fields.toMutableList()

    while (fields.isNotEmpty()) {
        val fieldsParser = any(*fields.map { str(it.identifier) }.toTypedArray())
        val fieldTypeName = (ctx parse fieldsParser) ?: crash("Type constructor for $typeName missing field(s)")
        val fieldType = fields.first { it.identifier == fieldTypeName }

        (ctx parse equals)

        when (fieldType) {
            is TypeField16 -> {
                imm16
            }
            is TypeField8 -> {
                imm8
            }
            is TypeFieldCustom -> {
                typeDeclaration(types)
            }
        }

        (ctx parse str(',')) ?: crash("$typeName constructor missing semicolon")
    }

    (ctx parse str(')')) ?: crash("$typeName constructor missing close )")
    success(null)
}

fun customVarDefs(types: Set<Type>) = contextual { ctx ->
    val type = (ctx parse -identifier)

    if (type !in types.map { it.identifier }) {
        fail("Not a custom var declaration")
    }

    val name = (ctx parse sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] }) ?: crash("Error with custom var declaration")

    ctx.ifParseable(equals) {
        val words = (ctx parse any(singleWordList, multiWordList, wordString)) ?: crash("Error with = var word definition")
//        success(Var16Definition(name, words))
    }

    ctx.ifParseable(-word) { numWords ->
        val zero = 0.toShort()
        val words = List(numWords.toInt()) { zero }
//        success(Var16Definition(name, words))
    }

    crash("Var word definition doesn't have equals nor size")

    success(null)
}
