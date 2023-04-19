package org.toptobes.lang

import org.toptobes.lang.parsers.createInstructionParser
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.any
import java.io.File

data class InstructionMetadata(val mnemonic: String, val tag: String, val opcode: Byte, val size: Int, val parser: Parser<String, Instruction>)

private val argSizes = mapOf(
    "REG16" to 1,
    "REG8"  to 1,
    "IMM8"  to 1,
    "IMM16" to 2,
    "MEM"   to 2,
    "PTR"   to 1,
)

val instructions = File("../opcodes")
    .readText()
    .split(",")
    .asSequence()
    .filter(String::isNotBlank)
    .map { it.trim() }
    .fold(listOf<InstructionMetadata>()) { instructions, name ->
        val code = instructions.size.toByte()

        val nameAndArgs = name.split("_")

        val tag = name.uppercase()
        val mnemonic = nameAndArgs.first().uppercase()

        val args = nameAndArgs.drop(1).reversed().toTypedArray()
        val parser = createInstructionParser(nameAndArgs.first(), *args)

        val size = 1 + args.fold(0) { size, arg -> size + argSizes[arg.uppercase()]!! }

        instructions + InstructionMetadata(mnemonic, tag, code, size, parser)
    }
    .groupBy { it.mnemonic.lowercase() }

val instructionParsers = instructions
    .mapValues { (_, list) ->
        any(*list.map { it.parser }.toTypedArray())
    }

fun getInstructionMetadata(node: Node): InstructionMetadata? {
    if (node !is Instruction) {
        throw IllegalArgumentException("${node.javaClass.simpleName} isn't an Instruction ∴ doesn't have metadata")
    }

    val mnemonic = node.mnemonic.uppercase()

    val args = node.args
        .reversed()
        .joinToString(separator = "_") { it.operandAssociation }

    val tag = (mnemonic + "_" + args)
        .trimEnd('_')

    return instructions[node.mnemonic]?.first { it.tag == tag }
}
