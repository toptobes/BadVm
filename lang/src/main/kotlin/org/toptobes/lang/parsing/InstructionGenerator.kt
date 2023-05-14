@file:Suppress("LocalVariableName")

package org.toptobes.lang.parsing

import org.toptobes.lang.ast.Instruction
import org.toptobes.lang.ast.Operand
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.str
import java.io.File

/* TODO: More efficient instruction parser that doesn't backtrack as much */

data class InstructionMetadata(
    val mnemonic: String,
    val tag: String,
    val opcode: Byte,
    val size: Int,
    val numArgs: Int,
    val parser: Parser<Instruction>,
)

private val argSizes = mapOf(
    "REG16" to 1,
    "REG8"  to 1,
    "PTR"   to 1,
    "IMM8"  to 1,
    "IMM16" to 2,
    "MEM8"  to 2,
    "MEM16" to 2,
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
        val parser = createSingleInstructionParser(nameAndArgs.first(), *args)

        val size = 1 + args.fold(0) { size, arg -> size + argSizes[arg.uppercase()]!! }

        instructions + InstructionMetadata(mnemonic, tag, code, size, args.size, parser)
    }
    .groupBy { it.mnemonic.lowercase() }
    .mapValues { (_, variations) -> variations.sortedByDescending { it.numArgs } }

val instructionParsers = instructions
    .mapValues { (_, list) ->
        any(*list.map { it.parser }.toTypedArray())
    }

private fun createSingleInstructionParser(name: String, vararg args: String) = contextual {
    (ctx parse -str(name)) ?: crash("Not an instruction")

    val parsedArgs = args.foldIndexed(emptyList<Operand>()) { idx, acc, arg ->
        val parser = operandParserMap[arg.uppercase()] ?: crash("No parser found for arg #$idx '$arg' for $name")

        val parsed = (ctx parse parser) ?: fail("Error with arg #${idx + 1} ($arg) for $name")

        if (idx != args.size - 1) {
            (ctx parse -str(',')) ?: fail("No expected comma")
        }

        acc + parsed
    }

    succeed(Instruction(name, parsedArgs))
}

val instructionsParser = Parser { oldState ->
    val line = oldState.subtarget().substringBefore("\r\n").substringBefore("\n")
    val name = line.substringBefore(" ")

    val parser = instructionParsers[name] ?: return@Parser errored(oldState, "Instruction '$name' not found")
    val parsedState = parser.parsePropagating(oldState)

    if (parsedState.isErrored()) {
        throw DescriptiveParsingException("Bad args or format for line '$line'", parsedState)
    }

    return@Parser success(parsedState)
}
