@file:Suppress("LocalVariableName")

package org.toptobes.oldlang.mappings

import org.toptobes.oldlang.nodes.Identifiables
import org.toptobes.oldlang.nodes.Instruction
import org.toptobes.oldlang.nodes.Node
import org.toptobes.oldlang.nodes.Operand
import org.toptobes.oldlang.parsers.operandParserMap
import org.toptobes.parsercombinator.contextual
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.unaryMinus
import java.io.File

data class InstructionMetadata(
    val mnemonic: String,
    val tag: String,
    val opcode: Byte,
    val size: Int,
    val numArgs: Int,
    val parser: (Identifiables) -> contextual<Instruction>,
)

private val argSizes = mapOf(
    "REG16" to 1,
    "REG8"  to 1,
    "IMM8"  to 1,
    "IMM16" to 2,
    "MEM16" to 2,
    "MEM8"  to 2,
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

        instructions + InstructionMetadata(mnemonic, tag, code, size, args.size, parser)
    }
    .groupBy { it.mnemonic.lowercase() }
    .mapValues { (_, variations) -> variations.sortedByDescending { it.numArgs } }

val instructionParsers = instructions
    .mapValues { (_, list) ->
        { vars: Identifiables -> any(*list.map { it.parser(vars) }.toTypedArray()) }
    }

fun createInstructionParser(name: String, vararg args: String) = { vars: Identifiables ->
    contextual { ctx ->
        (ctx parse -str(name)) ?: crash("Not an instruction")

        val parsedArgs = args.foldIndexed(emptyList<Operand>()) { idx, acc, arg ->
            val _parser = operandParserMap[arg.uppercase()] ?: crash("No parser found for arg #$idx '$arg' for $name")
            val parser = _parser(vars)

            val parsed = (ctx parse parser) ?: fail("Error with arg #${idx + 1} ($arg) for $name: ${ctx.state.error?.rootCause()}")

            if (idx != args.size - 1) {
                (ctx parse -str(',')) ?: fail("No expected comma")
            }

            acc + parsed
        }

        succeed(Instruction(name, parsedArgs))
    }
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