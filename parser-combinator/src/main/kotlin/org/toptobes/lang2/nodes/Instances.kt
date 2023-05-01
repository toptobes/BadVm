package org.toptobes.lang2.nodes

import org.toptobes.lang2.utils.Word
import org.toptobes.toBytes

data class LabelDefinition(override val identifier: String) : VariableDefinition() {
    init { allocType = Embedded; isExport = false }
    override val size = 2

    override fun toString() = """
        "label $identifier": $identifier
    """.trimIndent()
}

data class ByteInstance(override val identifier: String, val byte: Byte) : StaticDefinition() {
    override fun toBytes() = listOf(byte)
    override val size = 1

    override fun toString() = """
        "byte $identifier": $byte
    """.trimIndent()
}

data class WordInstance(override val identifier: String, val word: Word) : StaticDefinition() {
    override fun toBytes() = word.toBytes()
    override val size = 2

    override fun toString() = """
        "word $identifier": $word
    """.trimIndent()
}

data class AddrInstance(override val identifier: String, val link: TypeAddrVariable) : StaticDefinition() {
    override fun toBytes() = throw IllegalAccessException("Can't turn addr instance into bytes")
    override val size = 2

    override fun toString() = """
        "addr $identifier": null
    """.trimIndent()
}

data class ByteArrayInstance(override val identifier: String, val bytes: List<Byte>) : VectorDefinition() {
    override val size = bytes.size

    override fun toString() = """
        "byte $identifier": $bytes
    """.trimIndent()
}

data class WordArrayInstance(override val identifier: String, val words: List<Word>) : VectorDefinition() {
    override val size = words.size

    override fun toString() = """
        "word $identifier": $words
    """.trimIndent()
}
