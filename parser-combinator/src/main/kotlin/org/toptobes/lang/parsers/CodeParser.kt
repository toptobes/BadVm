package org.toptobes.lang.parsers

import org.toptobes.lang.*
import org.toptobes.lang.utils.Either
import org.toptobes.parsercombinator.ErrorResult
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.until
import org.toptobes.parsercombinator.isErrored
import org.toptobes.parsercombinator.unaryMinus

private fun nodeParser(types: MutableMap<String, Type>): any<String, Node> {
    return any(instructionParser(), definitionParser(types), commentParser())
}

private fun codeParser(types: MutableMap<String, Type>): until<String, Node> {
    return until(-nodeParser(types), checkAtEndOfLoop = true) { it.target.length == it.index }
}

fun parseCode(instructions: String): Either<ErrorResult, List<Node>> {
    val types = mutableMapOf<String, Type>()
    val nodesState = codeParser(types)(instructions)

    return if (nodesState.isErrored) {
        Either.Left(nodesState.error!!)
    } else {
        Either.Right(nodesState.result!!.filter { node -> node !== DeleteThisNode })
    }
}
