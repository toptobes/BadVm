package org.toptobes.lang

import org.toptobes.lang.nodes.Node

fun encodeIr(ir: List<Node>): List<Byte> {
    val identifiers = mutableMapOf<String, Short>()
    val bytecode = mutableListOf<Byte>(0, 0)

//    ir.forEach { node -> encodePass1(bytecode, identifiers, node) }
//
//    ir.fold(bytecode.size) { acc, value -> encodePass2(identifiers, acc, value) }
//
//    val (high, low) = (identifiers["_start"] ?: bytecode.size.toShort()).toBytes()
//    bytecode[0] = high
//    bytecode[1] = low
//
//    ir.filterIsInstance<Instruction>()
//      .forEach { encodePass3(bytecode, identifiers, it) }
    return bytecode
}

//private fun encodePass1(bytecode: MutableList<Byte>, identifiers: MutableMap<String, Short>, node: Node) = when (node) {
//    is Var8Definition -> {
//        identifiers[node.identifier] = bytecode.size.toShort()
//        bytecode += node.bytes
//    }
//    is Var16Definition -> {
//        identifiers[node.identifier] = bytecode.size.toShort()
//        bytecode += node.words.flatMap(Short::toBytes)
//    }
//    is TypeInstance -> {
//        identifiers[node.identifier] = bytecode.size.toShort()
//        bytecode += node.toBytes()
//    }
//    else -> Unit
//}
//
//private fun encodePass2(identifiers: MutableMap<String, Short>, currentSize: Int, node: Node) = if (node is VariableDefinition) {
//    when (node) {
//        is LabelDefinition -> {
//            identifiers[node.identifier] = currentSize.toShort()
//        }
//        is Const8Definition -> {
//            identifiers[node.identifier] = node.byte.toShort()
//        }
//        is Const16Definition -> {
//            identifiers[node.identifier] = node.word
//        }
//    }
//    currentSize
//} else {
//    val metadata = getInstructionMetadata(node)!!
//    currentSize + metadata.size
//}
//
//private fun encodePass3(bytecode: MutableList<Byte>, identifiers: MutableMap<String, Short>, node: Instruction) {
//    val metadata = getInstructionMetadata(node)
//    metadata?.let { bytecode += it.opcode }
//
//    node.args.filterIsInstance<VariableUsage>().forEach {
//        val identifier = it.identifier.trimStart('$', '@', '&')
//        val value = identifiers[identifier] ?: throwIdentifiableUsedBeforeDefinition(it)
//
//        if (it is IdentifiableWord) {
//            it.actualValue = value
//        } else if (it is IdentifiableByte) {
//            it.actualValue = value.toByte()
//        }
//    }
//
//    node.args.forEach { when (it) {
//        is Register -> {
//            bytecode += it.code
//        }
//        is ImmediateWord -> {
//            bytecode += it.value.toBytes()
//        }
//        is ImmediateByte -> {
//            bytecode += it.value
//        }
//        is MemAddress -> {
//            bytecode += it.address.toBytes()
//        }
//        else -> throw IllegalStateException("Bad node ${it.name}")
//    }}
//}
//
//private fun throwIdentifiableUsedBeforeDefinition(identifiable: VariableUsage): Nothing {
//    throw IllegalStateException("${identifiable.identifier} (${identifiable.javaClass.simpleName}) used before definition")
//}
