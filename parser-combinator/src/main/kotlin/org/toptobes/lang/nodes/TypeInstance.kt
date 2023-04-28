@file:Suppress("FunctionName")

package org.toptobes.lang.nodes

import org.toptobes.lang.utils.StatelessParsingException

// -- INSTANCES --

data class TypeInstance(override val identifier: String, val type: DefinedType, val fields: List<StaticDefinition>) : StaticDefinition() {
    override var allocType: AllocationType = Allocated
        set(value) {
            fields.forEach { it.allocType = value }
            field = value
        }

    override val size = fields.fold(0) { acc, field -> acc + field.size }

    override fun toBytes(): List<Byte> = fields.fold(listOf()) { acc, field ->
        acc + field.toBytes()
    }

    override fun toString() = """
        { "${type.identifier} $identifier": $fields }
    """.trimIndent()
}

fun ZeroedTypeInstance(identifier: String, type: DefinedType): TypeInstance {
    val fields = type.declaredFields.map { when (it) {
        is TypeDefinitionFieldByte -> ByteInstance(it.fieldName, 0)
        is TypeDefinitionFieldWord -> WordInstance(it.fieldName, 0)
        is TypeDefinitionFieldType -> {
            val nestedType = it.typeFn()
            if (nestedType !is DefinedType) {
                throw StatelessParsingException("Trying to create an instance of a non-defined-type ${nestedType.identifier}")
            }
            ZeroedTypeInstance(it.fieldName, nestedType)
        }
        is TypeDefinitionFieldAddr -> WordInstance(it.fieldName, 0)
    }}

    return TypeInstance(identifier, type, fields)
}
