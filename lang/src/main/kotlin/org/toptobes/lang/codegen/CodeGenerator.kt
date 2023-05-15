package org.toptobes.lang.codegen

import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.toBytes
import org.toptobes.lang.utils.toBytesList
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.VarMap

fun encode(ir: List<AstNode>, vars: VarMap): List<Byte> {
    val varCode = encodeVars(vars)
    val mappedVars = mapLabels(ir, vars)
    val irCode = encodeIr(ir, mappedVars)
    val startAddr = (mappedVars["_start"] as? Label)?.address ?: vars.nextAddress
    return startAddr.toBytesList() + varCode + irCode
}

private fun encodeVars(vars: VarMap) = vars.vars.values.filterIsInstance<Variable>().map {
    it.allocatedBytes.toList()
}.flatten()

private fun mapLabels(ir: List<AstNode>, vars: VarMap): VarMap {
    val mappedVars = vars.toMutableMap()
    var currentAddr = vars.nextAddress.toInt()

    ir.forEach { when (it) {
        is Label -> (mappedVars[it.name] as Label).address = currentAddr.toWord()
        is Instruction -> currentAddr += it.metadata.size
        else -> Unit
    }}

    return VarMap(mappedVars, vars.nextAddress)
}

private fun encodeIr(ir: List<AstNode>, mappedVars: VarMap) = ir.map { node ->
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
