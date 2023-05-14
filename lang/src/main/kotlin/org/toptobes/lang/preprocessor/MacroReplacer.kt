package org.toptobes.lang.preprocessor

data class Macro(val name: String, val args: List<String>, val replaceFn: (List<String>) -> String)

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
                args.foldIndexed(lines) { idx, prevLines, arg ->
                    Regex("(\\W)?${arg}(\\W)?").replace(prevLines) { r -> r.groupValues[1] + replacedArgs[idx] + r.groupValues[2] }
                }
            }

            macros += Macro(name, args, replaceFn)

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
