@file:Suppress("ClassName", "PrivatePropertyName", "LocalVariableName")

package org.toptobes.lang2.parsers

import org.toptobes.lang2.mappings.instructionParsers
import org.toptobes.lang2.nodes.Identifiables
import org.toptobes.lang2.nodes.Instruction
import org.toptobes.lang2.utils.StatefulParsingException
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
