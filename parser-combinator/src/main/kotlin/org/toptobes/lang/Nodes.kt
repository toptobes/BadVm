package org.toptobes.lang

import org.toptobes.lang.utils.Word

interface Node

// -- INSTRUCTIONS --

data class Instruction(
    val mnemonic: String,
    val args: List<Operand>
) : Node

// -- OTHER SUB-INTERFACES --

interface Definition : Node {
    val identifier: String
}

interface Operand : Node {
    val operandAssociation: String
    val name: String
}

interface Identifiable {
    val identifier: String
}

interface IdentifiableWord : Identifiable {
    var actualValue: Short
}

interface IdentifiableByte : Identifiable {
    var actualValue: Byte
}

// -- IMMEDIATES --

interface ImmediateWord : Operand {
    override val operandAssociation get() = "IMM16"
    val value: Short
}

interface ImmediateByte : Operand {
    override val operandAssociation get() = "IMM8"
    val value: Byte
}

data class Imm16(override val value: Short) : ImmediateWord {
    override val operandAssociation = "IMM16"
    override val name = "IMM16"
}

data class Imm8(override val value: Byte) : ImmediateByte {
    override val name = "IMM8"
}

data class Const16(override val identifier: String) : ImmediateWord, IdentifiableWord {
    override var actualValue: Short = -1
    override val value get() = actualValue
    override val name = "CONST16"
}

data class Const8(override val identifier: String) : ImmediateByte, IdentifiableByte {
    override var actualValue: Byte = -1
    override val value get() = actualValue
    override val name = "CONST8"
}

data class Var(override val identifier: String) : ImmediateWord, IdentifiableWord {
    override var actualValue: Short = -1
    override val value get() = actualValue
    override val name = "VAR"
}

// -- REGISTERS --

interface Register : Operand {
    val code: Byte
}

data class Reg16(val regName: String) : Register {
    override val code: Byte = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG16"
    override val name = "REG16"
}

data class Reg8(val regName: String) : Register {
    override val code: Byte = reg8Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "REG8"
    override val name = "REG8"
}

data class Pointer(val regName: String) : Register {
    override val code: Byte = reg16Codes[regName] ?: throwInvalidRegister(regName)
    override val operandAssociation = "PTR"
    override val name = "PTR"
}

private fun throwInvalidRegister(regName: String): Nothing {
    throw IllegalArgumentException("Invalid register $regName")
}

// -- MEMORY --

interface MemAddress : Operand {
    override val operandAssociation get() = "MEM"
    val address: Short
}

data class ImmMem(override val address: Short) : MemAddress, Operand {
    override val name = "IMM_MEM"
}

data class ConstAsAddress(override val identifier: String) : MemAddress, IdentifiableWord {
    override var actualValue: Short = -1
    override val address get() = actualValue
    override val name = "CONST16"
}

data class Label(override val identifier: String) : MemAddress, IdentifiableWord {
    override var actualValue: Short = -1
    override val address get() = actualValue
    override val name = "LABEL"
}

// -- DEFINITIONS --

data class LabelDefinition(override val identifier: String) : Definition

data class Const8Definition(override val identifier: String, val byte: Byte) : Definition
data class Const16Definition(override val identifier: String, val word: Short) : Definition

data class Var8Definition(override val identifier: String, val bytes: List<Byte>) : Definition
data class Var16Definition(override val identifier: String, val words: List<Short>) : Definition

// -- STRUCTS --

interface Type : Definition {
    val size: Int
}

data class DeclaredType(override val identifier: String) : Type {
    override fun equals(other: Any?) = identifier == other
    override fun hashCode() = identifier.hashCode()
    override val size = -1
}

data class DefinedType(override val identifier: String, val declaredFields: List<DeclaredTypeField>) : Type {
    override fun equals(other: Any?) = identifier == other
    override fun hashCode() = identifier.hashCode()
    override val size = declaredFields.fold(0) { acc, field -> acc + field.size }
}

sealed interface DeclaredTypeField : Node {
    val fieldName: String
    val size: Int
}

data class DeclaredTypeFieldWord(override val fieldName: String) : DeclaredTypeField {
    override val size = 2
}

data class DeclaredTypeFieldByte(override val fieldName: String) : DeclaredTypeField {
    override val size = 1
}

data class DeclaredTypeFieldType(override val fieldName: String, val type: Type) : DeclaredTypeField {
    override val size = type.size
}

interface TypeInstance : Definition {
    fun toBytes(): List<Byte>
    val type: Type
    val size: Int
}

data class DefinedTypeInstance(override val identifier: String, override val type: Type, val fields: List<TypeField>) : TypeInstance {
    override val size = fields.fold(0) { acc, field -> acc + field.size }

    override fun toBytes() = fields.fold(listOf<Byte>()) { acc, field ->
        acc + when (field) {
            is TypeFieldByte -> listOf(field.value)
            is TypeFieldWord -> field.value.toBytes()
            is TypeFieldType -> field.type.toBytes()
        }
    }
}

data class ZeroedTypeInstance(override val identifier: String, override val type: Type) : TypeInstance {
    override val size = type.size

    override fun toBytes(): List<Byte> {
        return List(type.size) { 0 }
    }
}

sealed interface TypeField : Identifiable {
    val size: Int
}

data class TypeFieldWord(override val identifier: String, override val value: Word) : TypeField, ImmediateWord {
    override val name = "TYPE_FIELD_WORD"
    override val size = 2
}

data class TypeFieldByte(override val identifier: String, override val value: Byte) : TypeField, ImmediateByte {
    override val name = "TYPE_FIELD_BYTE"
    override val size = 1
}

data class TypeFieldType(override val identifier: String, val type: TypeInstance) : TypeField {
    override val size = type.size
}

// -- OTHER --

object DeleteThisNode : Node
