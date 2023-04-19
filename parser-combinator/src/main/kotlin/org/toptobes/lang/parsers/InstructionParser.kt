@file:Suppress("ClassName", "PrivatePropertyName")

package org.toptobes.lang.parsers

import org.toptobes.lang.Instruction
import org.toptobes.lang.Operand
import org.toptobes.lang.instructionParsers
import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.str

private val NEWLINE = System.getProperty("line.separator")!!

class instructionParser : Parser<String, Instruction>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out Instruction> {
        val name = oldState.target.substring(oldState.index).substringBefore(NEWLINE).substringBefore(" ")

        val parser = instructionParsers[name] ?: return errored(oldState, BasicErrorResult("Instruction '$name' not found"))

        val parsedState = parser.parsePropagating(oldState)
        if (parsedState.isErrored) {
            return errored(parsedState, BasicErrorResult("Bad args or format for line '${oldState.target}'"))
        }
        return parsedState
    }
}

fun createInstructionParser(name: String, vararg args: String) = contextual { ctx ->
    (ctx parse -str(name)) ?: crash("Not an instruction")

    val parsedArgs = args.foldIndexed(emptyList<Operand>()) { idx, acc, arg ->
        val parser = operandParserMap[arg.uppercase()] ?: crash("No parser found for arg #$idx '$arg' for $name")
        val parsed = (ctx parse parser) ?: fail("Error with arg #$idx ($arg) for $name: ${ctx.state.error?.rootCause()}")

        if (idx != args.size - 1) {
            (ctx parse -str(',')) ?: crash("No expected comma")
        }

        acc + parsed
    }

    success(Instruction(name, parsedArgs))
}
