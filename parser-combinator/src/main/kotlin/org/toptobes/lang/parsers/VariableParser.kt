@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsers

import org.toptobes.lang.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.str

fun variableDefinition(types: MutableMap<String, Type>) = contextual { ctx ->
    val typeName = ctx parse -identifier                                       or cfail("Not a variable declaration")

    val name = ctx parse org.toptobes.parsercombinator.impls.sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] }                                                               or ccrash("Error with @declaration name")

    val definition = ctx parse when (typeName) {
        "byte" -> {
            byteDefinition(name)
        }
        "word" -> {
            wordDefinition(name)
        }
        in types -> {
            typeDefinition(name, typeName, types)
        }
        else -> fail("Not a custom var declaration")
    }                                                                         or ccrash("Error with @$name declaration")

    success(definition)
}

private fun byteDefinition(name: String) = contextual { ctx ->
    ctx.ifParseable(-equals) {
        val bytes = ctx parse any(singleByteList, multiByteList, byteString) or ccrash("Error with @byte definition")
        success(Var8Definition(name, bytes))
    }

    ctx.ifParseable(-word) { numBytes ->
        val zero = 0.toByte()
        val bytes = List(numBytes.toInt()) { zero }
        success(Var8Definition(name, bytes))
    }

    crash("Variable byte definition doesn't have -equals nor size")
}

private fun wordDefinition(name: String) = contextual { ctx ->
    ctx.ifParseable(-equals) {
        val words = ctx parse any(singleWordList, multiWordList, wordString) or ccrash("Error with @word definition")
        success(Var16Definition(name, words))
    }

    ctx.ifParseable(-word) { numWords ->
        val zero = 0.toShort()
        val words = List(numWords.toInt()) { zero }
        success(Var16Definition(name, words))
    }

    crash("Variable byte definition doesn't have -equals nor size")
}

private fun typeDefinition(name: String, typeName: String, types: MutableMap<String, Type>) = contextual { ctx ->
    val type = types[typeName]!!

    ctx parse -equals                                                   or ccrash("@$name has no equals")

    ctx.ifParseable(-str("?")) {
        success(ZeroedTypeInstance(name, type))
    }

    val fields = ctx parse typeConstructor(type.identifier, types)      or ccrash("Error parsing @$name's fields")
    success(DefinedTypeInstance(name, type, fields))
}
