package org.toptobes.lang.ast

import org.toptobes.lang.utils.Word
import kotlin.properties.Delegates

sealed interface Bytes {
    val bytes: List<Bytes>
}

data class ImmediateBytes(override val bytes: List<Bytes>) : Bytes

data class AllocatedBytes(override val bytes: List<Bytes>) : Bytes {
    var address by Delegates.notNull<Word>()
}

interface Interpretation {
    val size: Int
}

object Word : Interpretation {
    override val size = 2
}

object Byte : Interpretation {
    override val size = 1
}

data class Field(val name: String, val offset: Int, val interpretation: Interpretation)

data class Type(val offsets: Map<String, Field>) : Interpretation {
    override val size = offsets.values.sumOf { it.offset }
}

val assumptions = mutableMapOf<String, Interpretation>()

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
