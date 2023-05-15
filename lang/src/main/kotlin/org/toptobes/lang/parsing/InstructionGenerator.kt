@file:Suppress("LocalVariableName")

package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
import org.toptobes.lang.utils.reg16Codes
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*
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
    "reg16" to 1,
    "reg8"  to 1,
    "ptr"   to 1,
    "imm8"  to 1,
    "imm16" to 2,
    "mem8"  to 2,
    "mem16" to 2,
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

        val tag = name.lowercase()
        val mnemonic = nameAndArgs.first()

        val args = nameAndArgs.drop(1).reversed().toTypedArray()
        val parser = createSingleInstructionParser(nameAndArgs.first(), *args)

        val size = 1 + args.fold(0) { size, arg -> size + argSizes[arg]!! }

        instructions + InstructionMetadata(mnemonic, tag, code, size, args.size, parser)
    }
    .groupBy { it.mnemonic.lowercase() }
    .mapValues { (_, variations) -> variations.sortedByDescending { it.numArgs } }

private val movPtrTypeParser = contextual {
    ctx parse -str("mov") orCrash "Not an instruction"

    ctx parse -str("<") orFail "Not a cast"
    val typeName = ctx parse -identifier orCrash "Cast* missing type"
    ctx parse -str(">") orCrash "Ptr cast missing >"

    val type = ctx.state.types[typeName] orCrash "$typeName is not a valid type"

    val names = ctx parse -sepByPeriods(-identifier, requireMatch = true) orFail "Missing identifier"

    if (names[0] !in reg16Codes.keys) {
        crash("${names[0]} is not a valid register")
    }

    val offset = getFieldOrTypeOffset(names.drop(1), type)

    ctx parse -str(",") orCrash "Missing comma"

    val reg2 = ctx parse reg16 orCrash "Error parsing 2nd register"

    succeed(Instruction("mov", listOf(reg2, RegPtr(names[0]), Imm16(offset.toWord()))))
}

val instructionParsers = instructions
    .mapValues { (mnemonic, list) ->
        if (mnemonic == "mov") {
            any(list.map { it.parser } + movPtrTypeParser)
        } else {
            any(list.map { it.parser })
        }
    }

private fun createSingleInstructionParser(name: String, vararg args: String) = contextual {
    ctx parse -str(name) orCrash "Not an instruction"

    val parsedArgs = args.foldIndexed(emptyList<Operand>()) { idx, acc, arg ->
        val parser = operandParserMap[arg] ?: crash("No parser found for arg #$idx '$arg' for $name")

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
        throw DescriptiveParsingException("Bad args or format", parsedState)
    }

    return@Parser success(parsedState)
}
