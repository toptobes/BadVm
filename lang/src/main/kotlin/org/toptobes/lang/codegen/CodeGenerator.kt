package org.toptobes.lang.codegen

import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.toBytesList
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.OkayParseState
import org.toptobes.parsercombinator.SymbolMap

fun encode(ps: OkayParseState<List<AstNode>>, instructionsStart: Int): Pair<List<Byte>, List<Byte>> {
    val mappedVars = mapLabels(ps, instructionsStart)
    val irCode = encodeIr(ps.result, mappedVars)
    return ps.allocations.toList() to irCode
}

private fun mapLabels(ps: OkayParseState<List<AstNode>>, instructionsStart: Int): SymbolMap {
    val (ir, symbols) = ps

    val mappedVars = symbols.toMutableMap()
    var currentAddr = instructionsStart

    ir.forEach { when (it) {
        is Label -> (mappedVars[it.name] as Label).address = currentAddr.toWord()
        is Instruction -> currentAddr += it.metadata.size
        else -> Unit
    }}

    return mappedVars
}

private fun encodeIr(ir: List<AstNode>, mappedVars: SymbolMap) = ir.map { node ->
    if (node === DeleteThisNode || node is Label) {
        return@map emptyList()
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
