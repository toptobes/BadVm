package org.toptobes.lang

import org.toptobes.lang.nodes.*
import org.toptobes.lang.parsers.*
import org.toptobes.lang.utils.Either
import org.toptobes.parsercombinator.ErrorResult
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.until
import org.toptobes.parsercombinator.isErrored
import org.toptobes.parsercombinator.unaryMinus

private fun nodeParser(vars: MutIdentifiables): any<String, Node> {
    return any(instructionParser(vars), labelDefinition, variableDefinition(vars), typeDefinition(vars), commentParser())
}

private fun codeParser(vars: MutIdentifiables): until<String, Node> {
    val nodeParser = nodeParser(vars)
    return until(-nodeParser, checkAtEndOfLoop = true) { it.target.length == it.index }
}

fun parseCode(instructions: String): Either<ErrorResult, List<Node>> {
    val vars = MutIdentifiables()
    val nodesState = codeParser(vars)(instructions)

    return if (nodesState.isErrored) {
        Either.Left(nodesState.error!!)
    } else {
        Either.Right(nodesState.result!!.filter { node -> node !== NodeToDelete })
    }
}
