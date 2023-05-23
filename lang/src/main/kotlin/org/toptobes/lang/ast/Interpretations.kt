@file:Suppress("ArrayInDataClass", "MemberVisibilityCanBePrivate")

package org.toptobes.lang.ast

import org.toptobes.parsercombinator.ParsingException

sealed interface Interpretation {
    val size: Int
}

object WordIntrp : Interpretation {
    override val size: Int = 2
}

object ByteIntrp : Interpretation {
    override val size: Int = 1
}

data class Vec(val intrp: Interpretation, override val size: Int) : Interpretation

data class Ptr(val intrp: Interpretation) : Interpretation {
    override val size: Int = 2
}

data class Field<T : Interpretation>(val name: String, val intrp: T, val offset: Int, val adjustMask: BooleanArray) {
    val size get() = intrp.size
    
    fun bytes(variable: Variable): ByteArray {
        return variable.bytes.copyOfRange(offset, offset + size)
    }
}

data class TypeIntrp(
    override val name: String,
    override val export: Boolean,
    val fields: Map<String, Field<*>>,
) : Symbol, Interpretation {
    override val size = fields.values.sumOf { it.intrp.size }

    fun ensureIsConcrete() {
        if (fields.isEmpty()) throw ParsingException("$name is a declared type, expected defined")
    }
}
