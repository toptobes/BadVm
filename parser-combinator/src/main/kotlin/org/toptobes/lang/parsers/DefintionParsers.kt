@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsers

import org.toptobes.lang.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun definitionParser() = any(labelDefinition, variableWordDef, variableByteDef, constWordDef, constByteDef)

// -- UTIL --

private val byteType = any(str("db"), str("byte"))
private val wordType = any(str("dw"), str("word"))

private fun <R> cStyleArrayOf(parser: Parser<String, R>): between<String, List<R>> {
    return between.curlyBrackets(sepBy.whitespaceInsensitiveCommas(parser))
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
