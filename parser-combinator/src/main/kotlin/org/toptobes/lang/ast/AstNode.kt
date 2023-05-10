package org.toptobes.lang.ast

interface AstNode

object DeleteThisNode : AstNode {
    override fun toString() = "*"
}
