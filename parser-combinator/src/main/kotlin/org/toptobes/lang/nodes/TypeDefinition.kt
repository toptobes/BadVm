package org.toptobes.lang.nodes

// -- DEFINITIONS --

data class DeclaredType(override val identifier: String) : TypeDefinition() {
    override fun equals(other: Any?) = identifier == other
    override fun hashCode() = identifier.hashCode()
    override val size = -1
}

data class DefinedType(override val identifier: String, val declaredFields: List<TypeDefinitionField>) : TypeDefinition() {
    override fun equals(other: Any?) = identifier == other
    override fun hashCode() = identifier.hashCode()
    override val size = declaredFields.fold(0) { acc, field -> acc + field.size }
}

// -- DEFINITION FIELDS --

sealed interface TypeDefinitionField : Node {
    val fieldName: String
    val size: Int
}

data class TypeDefinitionFieldAddr(override val fieldName: String) : TypeDefinitionField {
    override val size = 2
}

data class TypeDefinitionFieldWord(override val fieldName: String) : TypeDefinitionField {
    override val size = 2
}

data class TypeDefinitionFieldByte(override val fieldName: String) : TypeDefinitionField {
    override val size = 1
}

data class TypeDefinitionFieldType(override val fieldName: String, val typeFn: () -> TypeDefinition) : TypeDefinitionField {
    override val size get() = typeFn().size
}
