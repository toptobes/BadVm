package org.toptobes.oldlang

import org.toptobes.parsercombinator.DescriptiveParsingException
import org.toptobes.oldlang.utils.Either
import org.toptobes.parsercombinator.ParsingException
import org.toptobes.oldlang.utils.ifRight

fun compile(code: String): Either<String, List<Byte>> {
    try {
        val parsed = parseCode(code)

        val maybeError = (parsed as? Either.Left<String>)
            ?.value

        val bytecode = parsed.ifRight { ir ->
            encodeIr(ir)
        } ?: return Either.Left(maybeError!!)

        return Either.Right(bytecode)
    } catch (e: DescriptiveParsingException) {
        return Either.Left(e.message!!)
    } catch (e: ParsingException) {
        return Either.Left(e.message!!)
    }
}
