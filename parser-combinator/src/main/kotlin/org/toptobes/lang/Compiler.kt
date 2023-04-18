package org.toptobes.lang

import org.toptobes.lang.parsers.parseInstructions
import org.toptobes.parsercombinator.ErrorResult
import org.toptobes.utils.Either
import org.toptobes.utils.ifRight

fun compile(code: String): Either<ErrorResult, List<Byte>> {
    val parsed = parseInstructions(code)

    val bytecode = parsed.ifRight { ir ->
        encodeIr(ir)
    } ?: return (parsed as Either.Left)

    return Either.Right(bytecode)
}
