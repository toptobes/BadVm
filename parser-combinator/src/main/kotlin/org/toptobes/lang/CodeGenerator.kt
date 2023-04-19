package org.toptobes.lang

fun encodeIr(ir: List<Node>): List<Byte> {
    val identifiers = mutableMapOf<String, Short>()
    val bytecode = mutableListOf<Byte>(0, 0)

    ir.forEach { node -> encodePass1(bytecode, identifiers, node) }

    ir.fold(bytecode.size) { acc, value -> encodePass2(identifiers, acc, value) }

    val (high, low) = identifiers["_start"]?.toBytes() ?: throwNoStartProc()
    bytecode[0] = high
    bytecode[1] = low

    ir.filterIsInstance<Instruction>()
      .forEach { encodePass3(bytecode, identifiers, it) }
    return bytecode
}

private fun encodePass1(bytecode: MutableList<Byte>, identifiers: MutableMap<String, Short>, node: Node) = when (node) {
    is Var8Definition -> {
        identifiers[node.identifier] = bytecode.size.toShort()
        bytecode += node.bytes
    }
    is Var16Definition -> {
        identifiers[node.identifier] = bytecode.size.toShort()
        bytecode += node.words.flatMap(Short::toBytes)
    }
    else -> Unit
}

private fun encodePass2(identifiers: MutableMap<String, Short>, currentSize: Int, node: Node) = if (node is Definition) {
    when (node) {
        is LabelDefinition -> {
            identifiers[node.identifier] = currentSize.toShort()
        }
        is Const8Definition -> {
            identifiers[node.identifier] = node.byte.toShort()
        }
        is Const16Definition -> {
            identifiers[node.identifier] = node.word
        }
    }
    currentSize
} else {
    val metadata = getInstructionMetadata(node)!!
    currentSize + metadata.size
}

private fun encodePass3(bytecode: MutableList<Byte>, identifiers: MutableMap<String, Short>, node: Instruction) {
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

private fun throwNoStartProc(): Nothing {
    throw IllegalStateException("No _start prodecure found")
}

private fun throwIdentifiableUsedBeforeDefinition(identifiable: Identifiable): Nothing {
    throw IllegalStateException("${identifiable.identifier} (${identifiable.javaClass.simpleName}) used before definition")
}

private fun Short.toBytes(): List<Byte> {
    val high = (this.toInt() shr 8).toByte()
    val low  = this.toByte()
    return listOf(high, low)
}
