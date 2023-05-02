@file:Suppress("ClassName", "PrivatePropertyName", "LocalVariableName")

package org.toptobes.oldlang.parsers

import org.toptobes.oldlang.mappings.instructionParsers
import org.toptobes.oldlang.nodes.Identifiables
import org.toptobes.oldlang.nodes.Instruction
import org.toptobes.oldlang.utils.StatefulParsingException
import org.toptobes.parsercombinator.*

class instructionParser(val vars: Identifiables) : Parser<String, Instruction>() {
    override fun parse(oldState: ParseState<String, *>): ParseState<String, out Instruction> {
        val line = oldState.target.substring(oldState.index).substringBefore("\r\n").substringBefore("\n")
        val name = line.substringBefore(" ")

        val _parser = instructionParsers[name] ?: return errored(oldState, BasicErrorResult("Instruction '$name' not found"))
        val parser = _parser(vars)

        val parsedState = parser.parsePropagating(oldState)
        if (parsedState.isErrored) {
            throw StatefulParsingException("Bad args or format for line '$line'", parsedState)
        }
        return parsedState
    }
}
