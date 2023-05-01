package org.toptobes.lang.ast

interface AbstractTypeDefinition {
    val name: String
}

data class TypeDefinition(override val name: String, val fields: List<Field>) : AbstractTypeDefinition
data class TypeDeclaration(override val name: String) : AbstractTypeDefinition

sealed interface Field
data class ByteField(val name: String) : Field
data class WordField(val name: String) : Field
data class NestedTypeField(val name: String, val type: String) : Field
data class EmbeddedTypeField(val type: String) : Field
