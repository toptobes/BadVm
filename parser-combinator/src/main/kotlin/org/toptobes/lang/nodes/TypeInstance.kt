@file:Suppress("FunctionName")

package org.toptobes.lang.nodes

import org.toptobes.lang.utils.StatelessParsingException

// -- INSTANCES --

data class TypeInstance(override val identifier: String, val type: DefinedType, val fields: List<StaticDefinition>) : StaticDefinition() {
    override val size = fields.fold(0) { acc, field -> acc + field.size }

    override fun toBytes(): List<Byte> = fields.fold(listOf()) { acc, field ->
        acc + field.toBytes()
    }
}

fun ZeroedTypeInstance(identifier: String, type: DefinedType): TypeInstance {
    val fields = type.declaredFields.map { when (it) {
        is TypeDefinitionFieldByte -> ByteInstance("", 0)
        is TypeDefinitionFieldWord -> WordInstance("", 0)
        is TypeDefinitionFieldType -> {
            val nestedType = it.typeFn()
            if (nestedType !is DefinedType) {
                throw StatelessParsingException("Trying to create an instance of a non-defined-type ${nestedType.identifier}")
            }
            ZeroedTypeInstance("", nestedType)
        }
    }}

    return TypeInstance(identifier, type, fields)
}
