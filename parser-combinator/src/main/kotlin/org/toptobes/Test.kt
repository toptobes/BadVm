package org.toptobes

import org.toptobes.lang.nodes.*
import org.toptobes.lang.parsers.identifier
import org.toptobes.lang.parsers.variableDefinition
import org.toptobes.lang.utils.StatefulParsingException
import org.toptobes.lang.utils.StatelessParsingException
import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.ContextScope
import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.*
import org.toptobes.parsercombinator.unaryMinus

/**
 * ```
 * // myWord is replaced with "3" wherever myWord is used
 * // Can not be dereferenced
 * // i.e. 'mov rax, myWord' -> 'mov rax, 3'
 * imm word myWord = 3
 *
 * // Illegal
 * imm word myWord [numBytes] [initializer]
 * imm word myWord = { ... }
 *
 * // myWord is replaced with the address to the location of myWord
 * // and actually goes to the address when dereferenced
 * // i.e. 'mov ax, myWord ' -> 'mov ax, 0xff  '
 * // or   'mov ax, @myWord' -> 'mov ax, [0xff]'
 * word myWord = 3
 *
 * // myWord can be embedded in other declarations
 * // Can not be dereferenced
 * // Can not be used in an instruction
 * // i.e. 'word myOtherWord = { 0, ...myWord, 5 }`
 * embed word myWord = { 1, 2, 3 }
 *
 * // Legal
 * embed word myWord [numBytes] [initializer]
 * embed word myWord = { ... }
 *
 * // -------------------------------------------------------------
 *
 * type Byte =
 *   | byte b
 *
 * type Bytes =
 *   | byte b1
 *   | byte b2
 *
 * type Word =
 *   | word w
 *
 * type Words =
 *   | word w1
 *   | word w2
 *
 * type Node =
 *   | Word w
 *   | addr Node next
 *
 * // -------------------------------------------------------------
 *
 * // myByte is replaced with the (address + offset of field) of myByte
 * // and goes to the (address + offset of field) when dereferenced
 * // i.e. 'mov al, myByte.b ' -> 'mov al, 0xff + 0  '
 * // i.e. 'mov al, @myByte.b' -> 'mov al, [0xff + 0]'
 * Byte myByte = Byte{3}
 *
 * // myByte is replaced with the value of myByte @ the field offset
 * // Can not be dereferenced
 * // i.e. 'mov al, myByte.b ' -> 'mov al, 3'
 * imm Byte myByte = Byte{3}
 *
 * // myWords is replaced with the value of myByte @ the field offset
 * // Can not be dereferenced
 * // i.e. 'mov ax, myWords.w2 ' -> 'mov al, 8'
 * imm Words myWords = Words { w1: 4, w2: 8 }
 *
 * // myWord can be used directly inside of another type
 * // Can not be dereferenced
 * // Can not be used in an instruction
 * // i.e. 'Node { w: ...myWord, next: ? }
 * embed Word myWord = Word{1}
 *
 * // Legal
 * Node myNode1 = Node { Word{1}, next: ? }
 * Node myNode2 = Node { Word{2}, next: myNode1 }
 *
 * // Illegal
 * Node myNode2 = Node { Word{2}, next: @myNode1 }
 *
 * // Illegal
 * Word mN1Addr = Word { myNode1 }
 * Node myNode2 = Node { Word{2}, next: @mN1Addr.w }
 *
 * // Legal
 * embed Word mN1Addr = Word { myNode1 }
 * Node myNode2 = Node { Word{2}, next: ...mN1Addr }
 *
 * // Legal
 * imm Word mN1Addr = Word { myNode1 }
 * Node myNode2 = Node { Word{2}, next: mN1Addr.w }
 */
fun main() {
    val H = DefinedType("H", listOf(TypeDefinitionFieldWord("next")))
    val h = TypeInstance("h1", H, listOf(WordInstance("next", 0)))
    val haddr = AddrHolderVariable("h1", h)
    val a = WordInstance("h", 3).apply { allocType = Embedded }

    val vars = MutIdentifiables().apply {
        typeDefs += H
        varDefs += h
        varDefs += a
        varUsages += haddr
    }

    try {
        println(varUsage(vars)("h1").result)
    } catch (e: StatefulParsingException) {
        println(e.message!!)
    }
}

fun byteVarUsage(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstance<ByteOperand>()

fun wordVarUsage(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstance<WordOperand>()

fun byteAddrVarUsage(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstance<ByteAddrOperand>()

fun wordAddrVarUsage(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstance<WordAddrOperand>()

fun lateWordAddrVarUsage(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstance<LateInitWordOperand>()

fun embeddedVarUsage(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstance<EmbeddedBytesVariable>()

fun byteVarUsageOrCrash(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstanceCrashing<ByteOperand>()

fun wordVarUsageOrCrash(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstanceCrashing<WordOperand>()

fun byteAddrVarUsageOrCrash(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstanceCrashing<ByteAddrOperand>()

fun wordAddrVarUsageOrCrash(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstanceCrashing<WordAddrOperand>()

fun embeddedVarUsageOrCrash(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstanceCrashing<EmbeddedBytesVariable>()

fun lateWordAddrVarUsageOrCrash(nodes: Identifiables) = varUsage(nodes)
    .assureIsInstance<LateInitWordOperand>()

private inline fun <reified R> Parser<String, *>.assureIsInstance() = chain {
    (it as? R)?.let(::succeed) ?: fail("Expected ${R::class.simpleName}, got ${it?.javaClass?.simpleName}")
}

private inline fun <reified R> Parser<String, *>.assureIsInstanceCrashing() = map {
    (it as? R) ?: throw StatelessParsingException("Expected ${R::class.simpleName}, got ${it?.javaClass?.simpleName}")
}

fun varUsage(nodes: Identifiables) = contextual { ctx ->
    val (isEmbedded, isDereffed) = ctx parse getVarUsageMetadata() or ccrash("Error getting variable usage metadata")

    val definitions = ctx parse readVarUsage(nodes)                or cfail("Error reading the variable itself")
    val definition  = definitions.last()

    if (definition !is StaticDefinition) {
        throw StatelessParsingException("${definitions.last()} is not a static definition")
    }

    checkIfEmbedded(definition, isEmbedded)
    checkIfImmediate(definition, isDereffed)
    checkIfAllocated(definition, isDereffed)
    crash("Error using variable $definition")
}

private fun getVarUsageMetadata() = contextual { ctx ->
    val isEmbed = ctx canTryParse -str("...")
    val isDeref = ctx canTryParse -str("@")

    if (isEmbed && isDeref) {
        crash("Variable can't be both embedded and dereferenced")
    }

    success(isEmbed to isDeref)
}

private fun readVarUsage(nodes: Identifiables) = contextual { ctx ->
    val cascadingNames = ctx parse sepBy.periods(-identifier, allowTrailingSep = false, requireMatch = true) or cfail("No identifier found for variable usage")
    val firstName = cascadingNames[0]
    val first = nodes.varDefs[firstName]                                                                     or ccrash("No identifier with name ${cascadingNames[0]}")

    val variables = cascadingNames.drop(1).runningFold(firstName to first) { (prevName, variable), name ->
        if (variable !is TypeInstance) {
            crash("Trying to call $name on non-defined-type $prevName")
        }

        val next = variable.fields.firstOrNull { it.identifier == name }                                     or ccrash("No identifier with name $name")
        next.identifier to next
    }.map { it.second }

    success(variables)
}

private fun ContextScope<VariableUsage>.checkIfEmbedded(definition: StaticDefinition, isEmbedded: Boolean) = when {
    definition.allocType === Embedded && !isEmbedded -> {
        crash("Use of embedded variable ${definition.identifier} without '...'")
    }
    definition.allocType === Embedded -> {
        success(EmbeddedBytesVariable(definition.identifier, definition, definition.toBytes()))
    }
    isEmbedded -> {
        crash("Use of non-embedded variable ${definition.identifier} with embedding")
    }
    else -> null
}

private fun ContextScope<VariableUsage>.checkIfImmediate(definition: StaticDefinition, isDereffed: Boolean) = when {
    definition.allocType === Immediate && isDereffed -> {
        crash("Can not deref immediate variable ${definition.identifier}")
    }
    definition.allocType === Immediate && definition is ByteInstance -> {
        success(ByteVariable(definition.identifier, definition.byte))
    }
    definition.allocType === Immediate && definition is WordInstance -> {
        success(WordVariable(definition.identifier, definition.word))
    }
    definition.allocType === Immediate -> {
        crash("Invalid immediate type ${definition.javaClass.simpleName} ${definition.identifier}")
    }
    else -> null
}

private fun ContextScope<VariableUsage>.checkIfAllocated(definition: StaticDefinition, isDereffed: Boolean) = when {
    definition.allocType !== Allocated -> {
        crash("Var type is not allocated even though it should be wtf")
    }
    !isDereffed -> {
        success(AddrHolderVariable(definition.identifier, definition))
    }
    definition is TypeInstance -> {
        crash("Can not deref type instance directly")
    }
    definition is ByteInstance -> {
        success(ByteAddrVariable(definition.identifier, definition))
    }
    definition is WordInstance -> {
        success(WordAddrVariable(definition.identifier, definition))
    }
    else -> null
}

fun Word.toBytes(): List<Byte> {
    val high = (this.toInt() shr 8).toByte()
    val low  = this.toByte()
    return listOf(high, low)
}

fun List<Byte>.toWord(): Word {
    return ((this[0].toInt() shl 8) + this[1]).toShort()
}
