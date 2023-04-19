package org.toptobes.lang

import org.toptobes.lang.parsers.parseCode
import org.toptobes.parsercombinator.ErrorResult
import org.toptobes.lang.utils.Either
import org.toptobes.lang.utils.ifRight

fun compile(code: String): Either<ErrorResult, List<Byte>> {
    val parsed = parseCode(code)

    val bytecode = parsed.ifRight { ir ->
        encodeIr(ir)
    } ?: return (parsed as Either.Left)

    return Either.Right(bytecode)
}
