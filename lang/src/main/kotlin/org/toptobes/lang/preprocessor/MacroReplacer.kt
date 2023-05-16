package org.toptobes.lang.preprocessor

import org.toptobes.lang.ast.Macro
import kotlin.random.Random
import kotlin.random.nextULong

fun findMacros(str: String): Pair<String, MutableList<Macro>> {
    val identifier = "[_a-zA-Z]\\w*"

    val macros = mutableListOf<Macro>()

    val newStr = Regex("macro\\s+($identifier)\\s+((?:$identifier\\s+)*)\\s*=((?:\\s*\\|\\s*.*[^|\n])*)")
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
