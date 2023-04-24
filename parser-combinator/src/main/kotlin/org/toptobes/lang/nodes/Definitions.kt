package org.toptobes.lang.nodes

import org.toptobes.lang.utils.Word

sealed class Definition : Node, Identifiable {
    abstract val size: Int
    var isImmediate = false
    var isConstant  = false
    var isExport    = false
}

sealed class TypeDefinition     : Definition()
sealed class VariableDefinition : Definition()

sealed class StaticDefinition : VariableDefinition()
sealed class VectorDefinition : VariableDefinition()

data class LabelDefinition(override val identifier: String) : VariableDefinition() {
    init { isImmediate = true; isConstant = true; isExport = false }
    override val size = 2
}

data class ByteVarDefinition(override val identifier: String, val byte: Byte) : StaticDefinition() {
    override val size = 1
}

data class WordVarDefinition(override val identifier: String, val word: Word) : StaticDefinition() {
    override val size = 2
}

data class ByteArrayVarDefinition(override val identifier: String, val bytes: List<Byte>) : VectorDefinition() {
    override val size = bytes.size
}

data class WordArrayVarDefinition(override val identifier: String, val words: List<Word>) : VectorDefinition() {
    override val size = words.size
}
