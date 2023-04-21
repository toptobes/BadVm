package org.toptobes.lang

import org.toptobes.parsercombinator.ErrorResult
import org.toptobes.lang.utils.Either
import org.toptobes.lang.utils.StatefulParsingException
import org.toptobes.lang.utils.StatelessParsingException
import org.toptobes.lang.utils.ifRight

fun compile(code: String): Either<String, List<Byte>> {
    try {
        val parsed = parseCode(code)

        val maybeError = (parsed as? Either.Left<ErrorResult>)
            ?.value
            ?.rootCause()
            ?.prettyErrorMsg

        val bytecode = parsed.ifRight { ir ->
            encodeIr(ir)
        } ?: return Either.Left(maybeError!!)

        return Either.Right(bytecode)
    } catch (e: StatelessParsingException) {
        return Either.Left(e.message!!)
    } catch (e: StatefulParsingException) {
        return Either.Left(e.message!!)
    }
}
