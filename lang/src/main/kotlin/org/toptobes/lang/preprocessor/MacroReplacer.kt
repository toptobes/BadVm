package org.toptobes.lang.preprocessor

import org.toptobes.lang.ast.Macro
import org.toptobes.lang.parsing.identifierRegex
import kotlin.random.Random
import kotlin.random.nextULong

/**
 * The macro regex matches a macro of the format
 * ```
 * (export)? macro <Name> <arg1> <argN> =
 *  | <line1>
 *  | <lineN>
 * ```
 * Macros can use `RandName(seed)` to get a unique random
 * name every time it's called, which can always be referenced
 * by the same seed. Different calls have different seed shifters
 * so it's never the same (well at least in practice)
 */
fun findMacros(str: String): Pair<String, MutableList<Macro>> {
    val macros = mutableListOf<Macro>()

    val newStr = Regex("macro\\s+($identifierRegex)\\s+((?:$identifierRegex\\s+)*)\\s*=((?:\\s*\\|\\s*.*[^|\n])*)")
        .replace(str) {
            val name = it.groupValues[1]

            val args = it.groupValues[2]
                .split(' ')
                .map(String::trim)
                .filter(String::isNotBlank)

            val lines = it.groupValues[3]
                .split('|')
                .map(String::trim)
                .drop(1)
                .joinToString("\n")

            val replaceFn = { replacedArgs: List<String> ->
                val argsReplacedLines = args.foldIndexed(lines) { idx, prevLines, arg ->
                    Regex("(\\W)?${arg}(\\W)?").replace(prevLines) { r ->
                        r.groupValues[1] + replacedArgs[idx] + r.groupValues[2]
                    }
                }

                val seedShifter = Random.nextInt()

                val namesReplacedLines = Regex("RandName\\((\\d+?)\\)").replace(argsReplacedLines) { r ->
                    "_n" + Random(seedShifter + r.groupValues[1].toInt()).nextULong()
                }

                namesReplacedLines
            }

            macros += Macro(name, false, args, replaceFn)
            ""
        }

    return newStr to macros
}

fun replaceMacros(macros: List<Macro>, str: String) = macros.fold(str) { prevStr, macro ->
    val regex = Regex("${macro.name}\\((.*?)\\)")

    regex.replace(prevStr) {
        val args = it.groupValues[1]
            .split(",")
            .mapIndexed { idx, arg ->
                arg.substringAfter(macro.args[idx] + ":").trim()
            }

        macro.replaceFn(args)
    }
}
