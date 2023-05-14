package org.toptobes.lang.codegen

import org.toptobes.lang.ast.AstNode
import org.toptobes.lang.ast.DeleteThisNode
import org.toptobes.lang.ast.Instruction

fun encode(ir: List<AstNode>) = ir.map { node ->
    if (node === DeleteThisNode) {
        return@map emptyList()
    }

    if (node !is Instruction) {
        throw IllegalStateException("$node !is Instruction")
    }
    listOf(node.metadata.opcode) + node.operands.flatMap { it.bytes.toList() }
}.flatten()
