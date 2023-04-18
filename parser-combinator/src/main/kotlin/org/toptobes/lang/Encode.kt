package org.toptobes.lang

import kotlin.experimental.and

fun encodeIr(ir: List<Node>): List<Byte> {
    val identifiers = mutableMapOf<String, Short>()
    val bytecode = mutableListOf<Byte>(0, 0)

    ir.fold(2) { acc, value -> encodePass1(bytecode, identifiers, acc, value) }

    val (high, low) = bytecode.size.toShort().toBytes()
    bytecode[0] = high
    bytecode[1] = low

    ir.filterIsInstance<Instruction>()
      .forEach { encodePass2(bytecode, identifiers, it) }
    return bytecode
}

private fun encodePass1(bytecode: MutableList<Byte>, identifiers: MutableMap<String, Short>, currentSize: Int, node: Node) = when (node) {
    is LabelDefinition -> {
        identifiers[node.identifier] = currentSize.toShort()
        currentSize
    }
    is Var8Definition -> {
        identifiers[node.identifier] = currentSize.toShort()
        bytecode += node.bytes
        currentSize + node.bytes.size
    }
    is Var16Definition -> {
        identifiers[node.identifier] = currentSize.toShort()
        bytecode += node.words.flatMap(Short::toBytes)
        currentSize + (node.words.size * 2)
    }
    is Const8Definition -> {
        identifiers[node.identifier] = node.byte.toShort()
        currentSize
    }
    is Const16Definition -> {
        identifiers[node.identifier] = node.word
        currentSize
    }
    !is Instruction -> {
        throw IllegalArgumentException("Non instruction $node")
    }
    else -> {
        val metadata = getInstructionMetadata(node)!!
        currentSize + metadata.size
    }
}

private fun encodePass2(bytecode: MutableList<Byte>, identifiers: MutableMap<String, Short>, node: Instruction) {
    val metadata = getInstructionMetadata(node)
    metadata?.let { bytecode += it.opcode }

    node.args.filterIsInstance<Identifiable>().forEach {
        val identifier = it.identifier.trimStart('$', '@', '&')
        val value = identifiers[identifier] ?: throwIdentifiableUsedBeforeDefinition(it)

        if (it is Identifiable16) {
            it.actualValue = value
        } else if (it is Identifiable8) {
            it.actualValue = value.toByte()
        }
    }

    node.args.forEach { when (it) {
        is Register -> {
            bytecode += it.code
        }
        is Immediate16 -> {
            bytecode += it.value.toBytes()
        }
        is Immediate8 -> {
            bytecode += it.value
        }
        is MemAddress -> {
            bytecode += it.address.toBytes()
        }
        else -> throw IllegalStateException("Bad node ${it.name}")
    }}
}

private fun throwIdentifiableUsedBeforeDefinition(identifiable: Identifiable): Nothing {
    throw IllegalStateException("${identifiable.identifier} (${identifiable.javaClass.simpleName}) used before definition")
}

private fun Short.toBytes(): List<Byte> {
    val high = ((this and 0xff00.toShort()).toInt() shr 8).toByte()
    val low  = (this and 0x00ff).toByte()
    return listOf(high, low)
}
