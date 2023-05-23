package org.toptobes.lang.codegen

import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.toBytesList
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.OkayParseState
import org.toptobes.parsercombinator.SymbolMap

data class Segments(
    val data: IntArray,
    val code: IntArray,
)

fun encode(ps: OkayParseState<List<AstNode>>): Segments {
    val dataSeg = ps.allocations.toList()
    val (codeSeg, addrs) = encodeIr(ps.result, ps.symbols)
    return Segments(dataSeg, codeSeg, addrs)
}

private fun encodeIr(ir: List<AstNode>, symbols: SymbolMap) = ir.map { node ->
    if (node === DeleteThisNode) {
        return@map emptyList() to emptyList()
    }

    if (node !is Instruction) {
        throw IllegalStateException("$node !is Instruction")
    }

    listOf(node.metadata.opcode) + node.operands.flatMap {
        when (it) {
            is Lbl -> (mappedVars[it.name] as Label).address.toBytesList()
            else -> it.bytes.toList()
        }
    }
}.flatten()
