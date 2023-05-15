@file:Suppress("ArrayInDataClass", "MemberVisibilityCanBePrivate")

package org.toptobes.lang.ast

import org.toptobes.lang.utils.Word
import org.toptobes.parsercombinator.ParsingException
import kotlin.properties.Delegates

sealed interface Symbol: AstNode {
    val name: String
}

data class Constant(
    override val name: String,
    val bytes: ByteArray,
) : Symbol

data class Variable(
    override val name: String,
    val allocatedBytes: ByteArray,
) : Symbol {
    var address by Delegates.notNull<Word>()
}

data class Label(override val name: String) : Symbol {
    var address by Delegates.notNull<Word>()
}

sealed interface Interpretation {
    val size: Int
}

object WordInterpretation : Interpretation {
    override val size: Int = 2
}

object ByteInterpretation : Interpretation {
    override val size: Int = 1
}

data class Vec(val interpretation: Interpretation, override val size: Int) : Interpretation

data class Ptr(val interpretation: Interpretation) : Interpretation {
    override val size: Int = 2
}

data class Field<T : Interpretation>(val name: String, val interpretation: T, val offset: Int) {
    val size get() = interpretation.size
}

data class TypeInterpretation(val typeName: String, val fields: Map<String, Field<*>>) : Interpretation {
    override val size = fields.values.sumOf { it.interpretation.size }

    fun ensureIsConcrete() {
        if (fields.isEmpty()) throw ParsingException("$typeName is a declared type, expected defined")
    }
}
