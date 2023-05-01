package org.toptobes.lang.ast

interface AstNode

data class NamedNode<T : AstNode>(val name: String, val node: T) : AstNode

object DeleteThisNode : AstNode
