package org.toptobes.parsercombinator

import org.toptobes.lang.parsing.constructors.singleByte
import org.toptobes.lang.parsing.constructors.singleWord
import org.toptobes.lang.parsing.identifier
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.impls.*
import java.lang.NullPointerException

private val parserLookupTable = mutableMapOf<String, (List<Parser<String>>) -> Parser<String>>(
    "name" to { identifier },
    "word" to { singleWord..{ it.toWord().toString() } },
    "byte" to { singleByte..{ it[0].toString() } },
    "fields" to { sepBy(identifier, str("."))..{ s -> s.joinToString(".") } },
)

fun pcStrAddParser(name: String, parser: (List<Parser<String>>) -> Parser<String>) {
    parserLookupTable += name to parser
}

fun String.compilePc(): (Context) -> List<String> {
    val parsedState = compiler.invoke(this.trimIndent(), emptyMap())

    if (parsedState.isOkay()) {
       return { ctx: Context ->
           (ctx parse parsedState.result) ?: throw ContextualParseError(ctx.errorStr ?: "Err in pc str")
       }
    } else {
        throw ParsingException("Error parsing PC str (${parsedState.error})")
    }
}

private val compiledStrings = mutableMapOf<String, Parser<List<String>>>()

val untilNewLine = until(nextLetter) { it.ifOkay { result == "\n" } ?: true }
    .map { it.joinToString("") }

private val compiler: Parser<Parser<List<String>>> = contextual {
    val key = ctx parse untilNewLine orCrash "Error parsing key str"

    compiledStrings[key]?.let(::succeed)

    val fns = mutableListOf<ContextScope<List<String>>.(Context) -> String?>()
    val parsers = mutableMapOf<String, Parser<String>>()

    while (ctx.state.index < ctx.state.target.length) {
        ctx parse -str("[") orCrash "Missing ["
        val type = ctx parse (str("*") or str("-") or digits) orCrash "Missing parser type"
        ctx parse -str("]") orCrash "Missing ]"

        val parser = ctx parse parser(parsers) orCrash "Error parsing parser"

        val failFn = when {
            type.isInt()          -> ({ TODO() })
            ctx canPeek -str("[") -> ({ throw NullPointerException() })
            else -> ctx parse failFn orCrash "Error parsing fail fn"
        }

        when {
            type == "*" -> fns += {
                (ctx parse -parser) ?: failFn()
            }
            type == "-" -> fns += {
                (ctx parse -parser) ?: failFn(); null
            }
            type.isInt()  -> parsers += type to parser
            else -> TODO()
        }
    }

    val parser = contextual {
        val results = fns.mapNotNull { fn -> fn(this, ctx) }
        succeed(results)
    }

    compiledStrings[key] = parser
    succeed(parser)
}

private val strParser = betweenSingleQuotes(regex("[^']*"))..(::str)

private fun parserParser(parsers: Map<String, Parser<String>>): Parser<Parser<String>> = contextual {
    ctx parse -str("\\") orFail "Not a parser parser"
    val parserName = ctx parse -(identifier or digits) orCrash "Error parsing parser name"
    val args = ctx parse repeatedly(-parser(parsers)) orCrash "Error parsing parser args"

    succeed(when {
        parserName.isInt() -> parsers[parserName] ?: crash("No parser with id #$parserName")
        else -> parserLookupTable[parserName]?.let { p -> p(args) } ?: crash("No parser with name $parserName")
    })
}

private fun singleParser(parsers: Map<String, Parser<String>>) = contextual {
    val parser = ctx parse -any(
        strParser,
        parserParser(parsers),
    ) orFail "Error parsing single parser"

    val isOptional = ctx canParse -str("?")

    succeed(if (isOptional) parser withDefault "" else parser)
}

private fun parser(parsers: Map<String, Parser<String>>) = sepBy(
    -singleParser(parsers),
    str("|"),
    allowTrailingSep = false,
    requireMatch = true
).map(::any)

private val failFn: Parser<ContextScope<*>.() -> Nothing> = -sequence(
    -identifier,
    str(":"),
    -untilNewLine,
).map { (fnType, _, str) ->
    val ret: ContextScope<*>.() -> Nothing = when (fnType) {
        "crash" -> ({ crash(str) })
        "fail" -> ({ fail(str) })
        else -> TODO()
    }
    ret
}

private fun String.isInt() = toIntOrNull() != null
