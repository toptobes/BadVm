package org.toptobes.lang.ast

interface AbstractTypeDefinition {
    val name: String
}

data class TypeDefinition (override val name: String, val fields: List<Field>) : AbstractTypeDefinition

data class TypeDeclaration(override val name: String) : AbstractTypeDefinition

data class Field(val name: String, val type: Class<*>)
