package org.toptobes.lang.parsers

import org.toptobes.lang.Comment
import org.toptobes.lang.Node
import org.toptobes.lang.utils.Either
import org.toptobes.parsercombinator.ErrorResult
import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.impls.until
import org.toptobes.parsercombinator.isErrored
import org.toptobes.parsercombinator.unaryMinus

private val nodeParser = any(instructionParser(), definitionParser(), commentParser())
private val codeParser = until(-nodeParser, checkAtEndOfLoop = true) { it.target.length == it.index }

fun parseCode(instructions: String): Either<ErrorResult, List<Node>> {
    val nodesState = codeParser(instructions)

    return if (nodesState.isErrored) {
        Either.Left(nodesState.error!!)
    } else {
        Either.Right(nodesState.result!!.filter { node -> node !== Comment })
    }
}
