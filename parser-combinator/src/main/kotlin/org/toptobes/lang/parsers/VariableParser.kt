@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.lang.parsers

import org.toptobes.*
import org.toptobes.lang.nodes.*
import org.toptobes.lang.utils.UWord
import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun variableDefinition(nodes: MutIdentifiables) = contextual { ctx ->
    val modifiers = ctx parse pool(-str("alloc"), -str("embed"), -str("imm"), -str("export")) or ccrash("Error parsing variable keywords")
    val isAllocated = "alloc" in modifiers
    val isEmbedded  = "embed" in modifiers
    val isImmediate = "imm"   in modifiers

    if ((isAllocated != isEmbedded) && (isAllocated xor isEmbedded xor isImmediate)) {
        crash("Conflicting allocation type keywords")
    }

    val typeName = ctx parse -identifier or cfail("Not a @definition")

    val name = ctx parse -sequence(
        optionalWhitespace, !str("@"), identifier,
    )..{ it[2] } or ccrash("Error with @definition name")

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
        else -> fail("Not a custom type @definition")
    } or ccrash("Error with @$name definition")

    if (isImmediate && definition is VectorDefinition) {
        crash("Trying to assign a vector to an immediate variable $name")
    }

    definition.allocType = when {
        isEmbedded  -> Embedded
        isImmediate -> Immediate
        else -> Allocated
    }

    nodes.varDefs += name to definition

    success(definition)
}

private fun embeddedBytes(nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(embeddedVarUsage(nodes)) {
        success(it.value)
    }

    fail("Not an embedded bytes usage")
}

private fun byteVariableDefinition(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(between.squareBrackets(word..(Word::toString) then -str(",") then (byte..(Byte::toString) or str("it")))) { (n, _, init) ->
        val initializer = init.toByteOrNull() ?: if (init == "it") null else crash("Invalid initializer ($init) in byte array builder")

        val bytes = List(n.toInt()) { initializer ?: it.toByte() }
        success(ByteArrayInstance(name, bytes))
    }

    ctx parse -equals or ccrash("@byte definition $name doesn't have equals nor size")

    ctx.tryParse(cStyleArrayOf(any(embeddedBytes(nodes), byte..(::listOf), byteVarUsageOrCrash(nodes)..{ listOf(it.value) }))) {
        success(ByteArrayInstance(name, it.flatten()))
    }

    ctx.tryParse(byte) {
        success(ByteInstance(name, it))
    }

    ctx.tryParse(string) {
        success(ByteArrayInstance(name, it))
    }

    ctx.tryParse(byteVarUsage(nodes)) {
        success(ByteInstance(name, it.value))
    }

    crash("Error assigning @byte definition $name")
}

private fun embeddedWords(nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(embeddedVarUsage(nodes)) {
        if (it.value.size % 2 == 1) {
            crash("Trying to assign unaligned embedded bytes to word def")
        }
        success(it.value.windowed(2, 2).map(List<Byte>::toWord))
    }

    fail("Not an embedded words usage")
}

private fun wordVariableDefinition(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(-word) { numWords ->
        val initializer = ((ctx tryParse -word)?.toInt() ?: 0).toShort()
        val words = List(numWords.toInt()) { initializer }
        success(WordArrayInstance(name, words))
    }

    ctx.tryParse(between.squareBrackets(word..(Word::toString) then -str(",") then (word..(Word::toString) or str("it")))) { (n, _, init) ->
        val initializer = init.toShortOrNull() ?: if (init == "it") null else crash("Invalid initializer ($init) in word array builder")

        val words = List(n.toInt()) { initializer ?: it.toShort() }
        success(WordArrayInstance(name, words))
    }

    ctx parse -equals or ccrash("@word definition $name doesn't have equals nor size")

    ctx.tryParse(cStyleArrayOf(any(embeddedWords(nodes), word..(::listOf), wordVarUsageOrCrash(nodes)..{ listOf(it.value) }))) {
        success(WordArrayInstance(name, it.flatten()))
    }

    ctx.tryParse(word) {
        success(WordInstance(name, it))
    }

    crash("Error assigning @word definition $name")
}

private fun typeVariableDefinition(name: String, typeName: String, vars: Identifiables) = contextual { ctx ->
    val type = vars.typeDefs[typeName]!!

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

private fun <R> cStyleArrayOf(parser: Parser<String, R>): between<String, List<R>> {
    return between.curlyBrackets(sepBy.commas(parser))
}

private val string = between.doubleQuotes(until(char) { it.result == '"' })
    .map { it.map { chr -> chr.code.toByte() } }

// -- TYPE CONSTRUCTORS --

private fun typeConstructor(typeName: String, vars: Identifiables): Parser<String, List<StaticDefinition>> = contextual { ctx ->
    val type = vars.typeDefs[typeName]                                  or ccrash("'$typeName' is an invalid type name")

    if (type !is DefinedType) {
        crash("Undefined type thunk ${type.identifier} trying to be constructed")
    }

    ctx parse str(type.identifier)                                      or ccrash("'$typeName' constructor missing valid preceding name")

    ctx parse -str('{')                                                 or ccrash("'$typeName' constructor missing open {")

    val values = ctx parse parseConstructorArgs(type, vars)             or ccrash("'$typeName' error with parsing constructor args")

    ctx parse -str('}')                                                 or ccrash("'$typeName' constructor missing close }")

    success(values)
}

private fun parseConstructorArgs(type: DefinedType, vars: Identifiables) = contextual { ctx ->
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

        values += when (nextFieldType) {
            is TypeDefinitionFieldByte -> {
                val byte = (ctx parse imm8(vars)  or ccrash("Byte field '$nextFieldName' not being assigned byte in ${type.identifier} constructor")).value
                ByteInstance(nextFieldName, byte)
            }
            is TypeDefinitionFieldWord -> {
                val word = (ctx parse imm16(vars) or ccrash("Word field '$nextFieldName' not being assigned word in ${type.identifier} constructor")).value
                WordInstance(nextFieldName, word)
            }
            is TypeDefinitionFieldType -> {
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
