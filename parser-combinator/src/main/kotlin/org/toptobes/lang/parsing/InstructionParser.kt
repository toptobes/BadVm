package org.toptobes.lang.parsing

import org.toptobes.parsercombinator.*

val instructionParser = Parser { oldState ->
    val line = oldState.subtarget().substringBefore("\r\n").substringBefore("\n")
    val name = line.substringBefore(" ")

    val parser = instructionParsers[name] ?: return@Parser errored(oldState, "Instruction '$name' not found")

    val parsedState = parser.parsePropagating(oldState)

    if (parsedState.isErrored()) {
        throw DescriptiveParsingException("Bad args or format for line '$line'", parsedState)
    }

    return@Parser success(parsedState)
}
