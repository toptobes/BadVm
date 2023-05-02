package org.toptobes.lang.ast

import org.toptobes.lang.utils.Word
import kotlin.properties.Delegates

sealed interface Definition {
    val name: String
}

data class WordInstance(override val name: String, val word: Word) : Definition
data class ByteInstance(override val name: String, val byte: Byte) : Definition

data class EmbeddedBytesInstance(override val name: String, val bytes: List<Byte>) : Definition

data class WordAddrInstance(override val name: String, val words: List<Word>) : Definition {
    var address by Delegates.notNull<Word>()
}

data class ByteAddrInstance(override val name: String, val bytes: List<Byte>) : Definition {
    var address by Delegates.notNull<Word>()
}
