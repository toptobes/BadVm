@file:Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package org.toptobes.oldlang.parsers

import org.toptobes.*
import org.toptobes.oldlang.nodes.*
import org.toptobes.oldlang.utils.Word
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun variableDefinition(nodes: MutIdentifiables) = contextual { ctx ->
    val modifiers = ctx parse pool(-str("alloc"), -str("embed"), -str("imm"), -str("export")) or ccrash("Error parsing variable keywords")
    val isAllocated = "alloc" in modifiers || modifiers.isEmpty()
    val isEmbedded  = "embed" in modifiers
    val isImmediate = "imm"   in modifiers

    if ((isAllocated == isEmbedded) && (isAllocated xor isEmbedded xor isImmediate)) {
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
            typeVariableDefinition(name, typeName, nodes, isAllocated)
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

    succeed(definition)
}

private fun embeddedBytes(nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(embeddedVarUsage(nodes)) {
        succeed(it.value)
    }

    fail("Not an embedded bytes usage")
}

private fun byteArrayBuilder(name: String) = contextual { ctx ->
    ctx.tryParse(between.squareBrackets(word..(Word::toString) then -str(",") then (byte..(Byte::toString) or str("it")))) { (n, _, init) ->
        val initializer = init.toByteOrNull() ?: if (init == "it") null else crash("Invalid initializer ($init) in byte array builder")

        val bytes = List(n.toInt()) { initializer ?: it.toByte() }
        succeed(ByteArrayInstance(name, bytes))
    }

    fail("Not a byte array builder")
}

private fun singleByte(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(byte) {
        succeed(ByteInstance(name, it))
    }

    ctx.tryParse(byteVarUsage(nodes)) {
        succeed(ByteInstance(name, it.value))
    }

    ctx.tryParse(embeddedBytes(nodes)) {
        if (it.size == 1) {
            succeed(ByteInstance(name, it[0]))
        }
    }

    fail("Not a single byte")
}

private fun byteArray(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(cStyleArrayOf(any(embeddedBytes(nodes), byte..(::listOf), byteVarUsageOrCrash(nodes)..{ listOf(it.value) }))) {
        succeed(ByteArrayInstance(name, it.flatten()))
    }

    ctx.tryParse(string) {
        succeed(ByteArrayInstance(name, it))
    }

    fail("Not a byte array")
}

private fun byteVariableDefinition(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(byteArrayBuilder(name)) {
        succeed(it)
    }

    ctx parse -equals or ccrash("@byte definition $name doesn't have equals nor size")

    ctx.tryParse(singleByte(name, nodes)) {
        succeed(it)
    }

    ctx.tryParse(byteArray(name, nodes)) {
        succeed(it)
    }

    crash("Error assigning @byte definition $name")
}

private fun embeddedWords(nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(embeddedVarUsage(nodes)) {
        if (it.value.size % 2 == 1) {
            crash("Trying to assign unaligned embedded bytes to word def")
        }
        succeed(it.value.windowed(2, 2).map(List<Byte>::toWord))
    }

    fail("Not an embedded words usage")
}

private fun wordArrayBuilder(name: String) = contextual { ctx ->
    ctx.tryParse(between.squareBrackets(word..(Word::toString) then -str(",") then (word..(Word::toString) or str("it")))) { (n, _, init) ->
        val initializer = init.toShortOrNull() ?: if (init == "it") null else crash("Invalid initializer ($init) in word array builder")

        val words = List(n.toInt()) { initializer ?: it.toShort() }
        succeed(WordArrayInstance(name, words))
    }

    fail("Not a word array builder")
}

private fun singleWord(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(word) {
        succeed(WordInstance(name, it))
    }

    ctx.tryParse(wordVarUsage(nodes)) {
        succeed(WordInstance(name, it.value))
    }

    ctx.tryParse(embeddedWords(nodes)) {
        if (it.size == 1) {
            succeed(WordInstance(name, it[0]))
        }
    }

    fail("Not a single word")
}

private fun wordArray(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(cStyleArrayOf(any(embeddedWords(nodes), word..(::listOf), wordVarUsageOrCrash(nodes)..{ listOf(it.value) }))) {
        succeed(WordArrayInstance(name, it.flatten()))
    }

    fail("Not a word array")
}

private fun wordVariableDefinition(name: String, nodes: Identifiables) = contextual { ctx ->
    ctx.tryParse(wordArrayBuilder(name)) {
        succeed(it)
    }

    ctx parse -equals or ccrash("@word definition $name doesn't have equals nor size")

    ctx.tryParse(singleWord(name, nodes)) {
        succeed(it)
    }

    ctx.tryParse(wordArray(name, nodes)) {
        succeed(it)
    }

    crash("Error assigning @word definition $name")
}

fun addr(name: String, nodes: Identifiables, isAllocated: Boolean) = contextual { ctx ->
    ctx.tryParse(between.squareBrackets(word)) {
        succeed(WordInstance(name, it))
    }

    if (!isAllocated) {
        crash("Can't have a non-constant address field in a non-embedded type")
    }

    ctx.tryParse(typeAddrVarUsage(nodes)) {
        succeed(AddrInstance(name, it))
    }

    fail("Not an addr")
}

private fun typeVariableDefinition(name: String, typeName: String, vars: Identifiables, isAllocated: Boolean) = contextual { ctx ->
    val type = vars.typeDefs[typeName]!!

    if (type !is DefinedType) {
        crash("Trying to instantiate an undefined type (only declared)")
    }

    ctx parse -equals or ccrash("@$name has no equals")

    ctx.tryParse(-str("?")) {
        succeed(ZeroedTypeInstance(name, type))
    }

    val fields = ctx parse typeConstructor(type.identifier, vars, isAllocated) or ccrash("Error parsing @$name's fields")
    succeed(TypeInstance(name, type, fields))
}

// -- PRIMITIVE CONSTRUCTORS --

private fun <R> cStyleArrayOf(parser: Parser<String, R>): between<String, List<R>> {
    return between.curlyBrackets(sepBy.commas(parser))
}

private val string = between.doubleQuotes(until(char) { it.result == '"' })
    .map { it.map { chr -> chr.code.toByte() } }

// -- TYPE CONSTRUCTORS --

private fun typeConstructor(typeName: String, vars: Identifiables, isAllocated: Boolean): Parser<String, List<StaticDefinition>> = contextual { ctx ->
    val type = vars.typeDefs[typeName]                                   or ccrash("'$typeName' is an invalid type name")

    if (type !is DefinedType) {
        crash("Undefined type thunk ${type.identifier} trying to be constructed")
    }

    ctx parse str(type.identifier)                                       or ccrash("'$typeName' constructor missing valid preceding name")

    ctx parse -str('{')                                                  or ccrash("'$typeName' constructor missing open {")

    val values = ctx parse parseConstructorArgs(type, vars, isAllocated) or ccrash("'$typeName' error with parsing constructor args")

    ctx parse -str('}')                                                  or ccrash("'$typeName' constructor missing close }")

    succeed(values)
}

private fun parseConstructorArgs(type: DefinedType, vars: Identifiables, isAllocated: Boolean) = contextual { ctx ->
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
                ctx parse singleByte(nextFieldName, vars)        or ccrash("byte field '$nextFieldName' not being assigned word in ${type.identifier} constructor")
            }
            is TypeDefinitionFieldWord -> {
                ctx parse singleWord(nextFieldName, vars)        or ccrash("word field '$nextFieldName' not being assigned word in ${type.identifier} constructor")
            }
            is TypeDefinitionFieldAddr -> {
                ctx parse addr(nextFieldName, vars, isAllocated) or ccrash("word field '$nextFieldName' not being assigned word in ${type.identifier} constructor")
            }
            is TypeDefinitionFieldType -> {
                val constructor = typeConstructor(nextFieldType.typeFn().identifier, vars, isAllocated)

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

    succeed(values)
}

private fun nextUnnamedTypeConstructorField(fields: MutableList<TypeDefinitionField>) = contextual { ctx ->
    val field = fields.removeFirst()
    val fieldName = field.fieldName

    (ctx tryParse -strOf(fieldName, -str(":")))
    succeed(field)
}

private fun nextNamedTypeConstructorField(type: DefinedType, fields: MutableList<TypeDefinitionField>) = contextual { ctx ->
    val fieldNameParser = -any(*fields.map { str(it.fieldName) }.toTypedArray())
    val fieldName = ctx parse fieldNameParser or ccrash("Type constructor for '${type.identifier}' missing field(s)")
    val fieldTypeIdx = fields.indexOfFirst { it.fieldName == fieldName }

    val fieldType = fields[fieldTypeIdx]
    fields.removeAt(fieldTypeIdx)

    (ctx parse -str(":")) or ccrash("Named type constructor for '${type.identifier}' missing a :")
    succeed(fieldType)
}
