@file:Suppress("LocalVariableName")

package org.toptobes.lang.parsing

import org.toptobes.lang.ast.*
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

        val args = nameAndArgs.drop(1).toTypedArray()
        val parser = createSingleInstructionParser(nameAndArgs.first(), *args)

        val size = 1 + args.fold(0) { size, arg -> size + argSizes[arg]!! }

        instructions + InstructionMetadata(mnemonic, tag, code, size, args.size, parser)
    }
    .groupBy { it.mnemonic.lowercase() }
    .mapValues { (_, variations) -> variations.sortedByDescending { it.numArgs } }

private val movPtrTypeParser = contextual {
    val (reg1, typeName, reg2, fields) = """
        mov reg1, <type ptr reg2>.fields
        [-] 'mov'            crash: Not an instruction
        [*] \name            fail:  Missing 2nd register
        [-] ','              crash: Missing comma
        [-] '<'              fail:  Not a cast
        [*] \name            crash: Cast missing type
        [-] 'ptr'            crash: Cast isn't casting to ptr
        [*] \name            crash: Cast missing register
        [-] '>'              crash: Cast missing >
        [-] '.'              crash: Missing fields
        [*] \fields          crash: Error parsing fields
    """.compilePc()(ctx)

    val type = ctx.lookup<TypeIntrp>(typeName)!!

    val offset = getFieldOrTypeOffset(fields.split(","), type)

    succeed(Instruction("mov", listOf(Reg16(reg1), RegPtr(reg2), Imm16(offset.toWord()))))
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

        val operand = (ctx parse parser) ?: fail("Error with arg #${idx + 1} ($arg) for $name")

        if (idx != args.size - 1) {
            (ctx parse -str(',')) ?: fail("No expected comma")
        }

        acc + operand
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
