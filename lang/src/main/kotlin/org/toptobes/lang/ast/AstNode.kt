package org.toptobes.lang.ast

sealed interface AstNode

object DeleteThisNode : AstNode {
    override fun toString() = "*"
}
