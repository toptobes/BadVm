@file:Suppress("ArrayInDataClass")

package org.toptobes.lang.ast

import org.toptobes.lang.utils.Word

sealed interface Symbol: AstNode {
    val name: String
    val export: Boolean
}

data class Variable(
    override val name: String,
    override val export: Boolean,
    val intrp: Interpretation,
    val bytes: ByteArray,
) : Symbol

data class Label(
    override val name: String,
    override val export: Boolean,
    var address: Word = -1,
) : Symbol

data class Macro(
    override val name: String,
    override val export: Boolean,
    val args: List<String>,
    val replaceFn: (List<String>) -> String
) : Symbol

/* data class TypeInterpretation: Symbol, Interpretation */
