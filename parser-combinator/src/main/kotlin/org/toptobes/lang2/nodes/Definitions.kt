package org.toptobes.lang2.nodes

sealed interface AllocationType
object Allocated : AllocationType
object Embedded  : AllocationType
object Immediate : AllocationType

sealed class Definition : Node, Identifiable {
    abstract val size: Int
    var isExport = false
}

sealed class TypeDefinition : Definition()

sealed class VariableDefinition : Definition() {
    open var allocType: AllocationType = Allocated
}

sealed class StaticDefinition : VariableDefinition() {
    val parent: StaticDefinition? = null
    abstract fun toBytes(): List<Byte>
}

sealed class VectorDefinition : VariableDefinition()
