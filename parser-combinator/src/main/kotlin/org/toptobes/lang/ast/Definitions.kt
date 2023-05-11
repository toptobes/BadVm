@file:Suppress("ArrayInDataClass")

package org.toptobes.lang.ast

import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.ParsingException
import kotlin.properties.Delegates

interface Definition<T : Bytes> {
    val name: String
}

data class Constant(override val name: String, val bytes: ImmediateBytes) : Definition<ImmediateBytes>

data class Variable(override val name: String, val bytes: PromisedBytes) : Definition<PromisedBytes>

sealed interface Bytes : AstNode {
    val bytes: ByteArray
    val interpretation: (String) -> Interpretation
}

data class ImmediateBytes(
    override val bytes: ByteArray,
    override val interpretation: (String) -> Interpretation
) : Bytes

data class PromisedBytes(
    val bytesSupplier: () -> ByteArray,
    override val interpretation: (String) -> Interpretation
) : Bytes {
    override val bytes: ByteArray
        get() = bytesSupplier()
}

data class BytesToAllocate(val bytes: ByteArray) {
    var address by Delegates.notNull<Word>()
}

interface Interpretation {
    val size: Word
}

object WordInterpretation : Interpretation {
    override val size: Word = 2
}

object ByteInterpretation : Interpretation {
    override val size: Word = 1
}

class Vec<T : Interpretation>(size: Number) : Interpretation {
    override val size: Word = size.toWord()
}

class Ptr<T : Interpretation>() : Interpretation {
    override val size: Word = 2
}

data class Field(val name: String, val interpretation: Interpretation)

data class TypeInterpretation(val typeName: String, val fields: Map<Word, Field>) : Interpretation {
    override val size = fields.keys.sum().toWord()

    fun ensureIsConcrete() {
        if (fields.isEmpty()) throw ParsingException("$typeName is a declared type, expected defined")
    }
}

//data class VarDefinition(
//    val name: String,
//    val bytes: List<Byte>,
//    val allocType: AllocationType,
//    val varType: VariableType,
//) : Argument {
//    override fun toString() = """
//        [$varType, $bytes]
//    """.trimIndent()
//}
//
//sealed interface AllocationType
//object Allocated : AllocationType
//object Embedded  : AllocationType
//object Immediate : AllocationType
//
//sealed interface VariableType
//object Bytes : VariableType { override fun toString() = "Bytes" }
//object Words : VariableType { override fun toString() = "Words" }
//data class OperandType<T : Operand>(val clazz: Class<T>) : VariableType { override fun toString() = clazz.simpleName!! }
//inline fun <reified T : Operand> OperandType() = OperandType(T::class.java)
//
//sealed interface TypeDefinition {
//    val name: String
//}
//
//data class ConcreteTypeDefinition(override val name: String, val fields: List<Field>) : TypeDefinition {
//    init {
//        val fieldNames = fields.map { it.name }.toMutableList()
//        val fieldNamesSet = fieldNames.toMutableSet()
//
//        if (fieldNames.size != fieldNamesSet.size) {
//            fieldNames.removeIf { fieldNamesSet.remove(it) }
//            throw ParsingException("Duplicate fields: $fieldNames")
//        }
//    }
//
//    override fun toString() = """
//        {typeName=$name, fields=$fields}
//    """.trimIndent()
//}
//
//data class DeclaredTypeDefinition(override val name: String) : TypeDefinition {
//    override fun toString() = """
//        {typeName=$name}
//    """.trimIndent()
//}
//
//sealed interface Field {
//    val name: String
//}
//
//data class ByteField(override val name: String) : Field { override fun toString() = "\"byte $name\"" }
//data class WordField(override val name: String) : Field { override fun toString() = "\"word $name\"" }
//data class NestedTypeField(override val name: String, val type: String) : Field { override fun toString() = "\"$type $name\"" }
//
//typealias Vars = Map<String, VarDefinition>
//typealias Types = Map<String, TypeDefinition>
//
//operator fun Vars.plus(def: VarDefinition): Map<String, VarDefinition> {
//    if (def.name in this) {
//        throw ParsingException("Duplicate variable definition ${def.name}")
//    }
//
//    return this + (def.name to def)
//}
//
//operator fun Types.plus(def: TypeDefinition): Map<String, TypeDefinition> {
//    if (def.name in this && (def is DeclaredTypeDefinition || this[def.name] is ConcreteTypeDefinition)) {
//        throw ParsingException("Duplicate type definition/declaration ${def.name}")
//    }
//
//    return this + (def.name to def)
//}
//
//fun TypeDefinition.ensureIsConcrete() {
//    contract {
//        returns() implies (this@ensureIsConcrete is ConcreteTypeDefinition)
//    }
//
//    if (this !is ConcreteTypeDefinition) {
//        throw ParsingException("Trying to use non-concrete type ${this.name}")
//    }
//}
