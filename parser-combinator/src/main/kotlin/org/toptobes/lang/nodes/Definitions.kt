package org.toptobes.lang.nodes

import org.toptobes.lang.utils.Word
import org.toptobes.toBytes

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
    var allocType: AllocationType = Allocated
}

sealed class StaticDefinition : VariableDefinition() {
    val parent: StaticDefinition? = null
    abstract fun toBytes(): List<Byte>
}

sealed class VectorDefinition : VariableDefinition()

data class LabelDefinition(override val identifier: String) : VariableDefinition() {
    init { allocType = Embedded; isExport = false }
    override val size = 2
}

data class ByteInstance(override val identifier: String, val byte: Byte) : StaticDefinition() {
    override fun toBytes() = listOf(byte)
    override val size = 1
}

data class WordInstance(override val identifier: String, val word: Word) : StaticDefinition() {
    override fun toBytes() = word.toBytes()
    override val size = 2
}

data class ByteArrayInstance(override val identifier: String, val bytes: List<Byte>) : VectorDefinition() {
    override val size = bytes.size
}

data class WordArrayInstance(override val identifier: String, val words: List<Word>) : VectorDefinition() {
    override val size = words.size
}
