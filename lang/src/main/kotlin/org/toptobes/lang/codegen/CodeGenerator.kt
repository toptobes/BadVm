package org.toptobes.lang.codegen

import org.toptobes.lang.DATA_SEGMENT_START_OFFSET
import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.toBytesList
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.SymbolMap

fun encode(ir: List<AstNode>, symbols: SymbolMap, allocated: ByteArray): List<Byte> {
    val mappedVars = mapLabels(ir, symbols, allocated)
    val irCode = encodeIr(ir, mappedVars)
    val startAddr = (mappedVars["_start"] as? Label)?.address ?: (allocated.size + DATA_SEGMENT_START_OFFSET)
    return startAddr.toBytesList() + allocated.toList() + irCode
}

private fun mapLabels(ir: List<AstNode>, symbols: SymbolMap, allocated: ByteArray): SymbolMap {
    val mappedVars = symbols.toMutableMap()
    var currentAddr = allocated.size + DATA_SEGMENT_START_OFFSET

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
