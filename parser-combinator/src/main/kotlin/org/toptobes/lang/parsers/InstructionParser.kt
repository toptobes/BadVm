package org.toptobes.lang.parsers

import org.toptobes.lang.*
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.str
import org.toptobes.utils.Either

val parserMap = mapOf(
    REG16 to reg16,
    REG8  to reg8,
    IMM16 to imm16,
    IMM8  to imm8,
    MEM   to mem,
    PTR   to ptr,
)

fun parseInstructions(instructions: String) : Either<ErrorResult, List<Node>> =
    instructions
        .split("\n")
        .filter(String::isNotBlank)
        .map(String::trim)
        .filter { !it.startsWith("--") }
        .map { it.substringBefore("--") }
        .map { any(instructionParser(), labelDefinition, variableWordDef, variableByteDef, constWordDef, constByteDef)(it) }
        .map { if (it.isErrored) return Either.Left(it.error!!) else it.result!! }
        .let { Either.Right(it) }

class instructionParser : Parser<String, Instruction>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out Instruction> {
        val name = oldState.target.substringBefore(" ")
        val parser = instructionParsers[name] ?: return errored(oldState, BasicErrorResult("Instruction '$name' not found"))

        val parsedState = parser.parsePropagating(oldState)
        if (parsedState.isErrored) {
            return errored(parsedState, BasicErrorResult("Bad args or format for line '${oldState.target}'"))
        }
        return parsedState
    }
}

fun createInstructionParser(name: String, vararg args: String) = contextual { ctx ->
    (ctx parse -str(name)) ?: error("Not a mov instruction")

    val parsedArgs = args.foldIndexed(emptyList<Operand>()) { idx, acc, arg ->
        val parser = parserMap[arg.uppercase()] ?: error("No parser found for instruction $arg")
        val parsed = (ctx parse parser) ?: error("Error with arg #$idx: ${ctx.state.error}")

        if (idx != args.size - 1) {
            (ctx parse -str(',')) ?: error("No expected comma")
        }

        acc + parsed
    }

    success(Instruction(name, parsedArgs))
}
