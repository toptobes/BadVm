package org.toptobes.lang.nodes

import org.toptobes.lang.utils.Word

interface Node

// -- OTHER SUB-INTERFACES --

sealed class Definition : Node {
    abstract val identifier: String
    abstract val size: Int
    var isImmediate = false
    var isConstant = false
    var isExport = false
}

sealed class VariableDefinition : Definition()

sealed class StaticDefinition : VariableDefinition()
sealed class VectorDefinition : VariableDefinition()
sealed class TypeDefinition   : Definition()

interface Operand : Node {
    val operandAssociation: String
    val name: String
}

interface VariableUsage {
    val identifier: String
}

interface WordVarUsage : VariableUsage {
    var actualValue: Word?
}

interface ByteVarUsage : VariableUsage {
    var actualValue: Byte?
}

// -- IMMEDIATES --

interface ImmediateWord : Operand {
    override val operandAssociation get() = "IMM16"
    val value: Word
}

interface ImmediateByte : Operand {
    override val operandAssociation get() = "IMM8"
    val value: Byte
}

data class Imm16(override val value: Word) : ImmediateWord {
    override val operandAssociation = "IMM16"
    override val name = "IMM16"
}

data class Imm8(override val value: Byte) : ImmediateByte {
    override val name = "IMM8"
}

data class WordVariable(override val identifier: String) : ImmediateWord, WordVarUsage {
    override var actualValue: Word? = null
    override val value get() = actualValue ?: throw IllegalStateException("Trying to get $identifier value before it being set")
    override val name = "VAR"
}

data class ByteVariable(override val identifier: String) : ImmediateByte, ByteVarUsage {
    override var actualValue: Byte? = null
    override val value get() = actualValue ?: throw IllegalStateException("Trying to get $identifier value before it being set")
    override val name = "VAR"
}

// -- MEMORY --

interface MemAddress : Operand {
    override val operandAssociation get() = "MEM"
    val address: Word
}

data class ImmMemAddress(override val address: Word) : MemAddress, Operand {
    override val name = "IMM_MEM"
}

data class AddressDefinition(override val identifier: String) : MemAddress, WordVarUsage, StaticDefinition() {
    override var actualValue: Word? = null
    override val address get() = actualValue ?: throw IllegalStateException("Trying to get $identifier value before it being set")
    override val name = "CONST16"
    override val size = 2
}

data class Label(override val identifier: String) : MemAddress, WordVarUsage {
    override var actualValue: Word? = null
    override val address get() = actualValue ?: throw IllegalStateException("Trying to get $identifier value before it being set")
    override val name = "LABEL"
}

// -- DEFINITIONS --

data class LabelDefinition(val identifier: String) : Node

data class ByteVarDefinition(override val identifier: String, val byte: Byte) : StaticDefinition() {
    override val size = 1
}

data class WordVarDefinition(override val identifier: String, val word: Word) : StaticDefinition() {
    override val size = 2
}

data class MultiByteVarDefinition(override val identifier: String, val bytes: List<Byte>) : VectorDefinition() {
    override val size = bytes.size
}

data class MultiWordVarDefinition(override val identifier: String, val words: List<Word>) : VectorDefinition() {
    override val size = words.size
}


// -- OTHER --

object NodeToDelete : Node
