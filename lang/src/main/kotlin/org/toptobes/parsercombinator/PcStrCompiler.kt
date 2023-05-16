package org.toptobes.parsercombinator

import org.toptobes.lang.parsing.identifier
import org.toptobes.parsercombinator.impls.*

fun String.compilePc(): (Context) -> List<String> {
    val parsedState = compiler.invoke(this, emptyMap())

    if (parsedState.isOkay()) {
       return { ctx: Context ->
           val result = ctx parse parsedState.result
           result ?: throw ParsingException(ctx.errorStr ?: "Err in pc str")
       }
    } else {
        throw ParsingException("Error parsing PC str (${parsedState.error})")
    }
}

private val compiledStrings = mutableMapOf<String, Parser<List<String>>>()

private val compiler: Parser<Parser<List<String>>> = contextual {
    val key = ctx parse regex("[^*-]*") orCrash "Error parsing key str"

    compiledStrings[key]?.let(::succeed)

    val fns = mutableListOf<ContextScope<List<String>>.(Context) -> String?>()

    while (ctx.state.index < ctx.state.target.length) {
        val discard = ctx parse (-str("*") or -str("-"))
        ctx parse -str(")") orCrash "Missing parenthesis"

        val parser = ctx parse parser orCrash "Error parsing parser"
        val failFn = ctx parse failFn orCrash "Error parsing fail fn"

        fns += {
            val result = (ctx parse -parser) ?: failFn()
            if (discard == "-") null else result
        }
    }

    val parser = contextual {
        val results = fns.mapNotNull { fn ->
            fn(this, ctx) }
        succeed(results)
    }

    compiledStrings[key] = parser
    succeed(parser)
}

private val strParser = betweenSingleQuotes(regex("[^']*"))..(::str)

private val parserParser = contextual {
    ctx parse -str("\\") orFail "Not a parser parser"
    val parser = ctx parse -identifier orCrash "Error parsing parser name"

    succeed(when (parser) {
        "name" -> identifier
        "fields" -> sepBy(identifier, str("."))..{ s -> s.joinToString(".") }
        else -> TODO()
    })
}

private val parser: Parser<Parser<String>> = -any(
    strParser,
    parserParser,
)

private val failFn: Parser<ContextScope<*>.() -> Nothing> = -sequence(
    -identifier,
    -str(":"),
    regex("[^*-]*")
).map { (fnType, _, str) ->
    val ret: ContextScope<*>.() -> Nothing = when (fnType) {
        "crash" -> ({ crash(str) })
        "fail" -> ({ fail(str) })
        else -> TODO()
    }
    ret
}
