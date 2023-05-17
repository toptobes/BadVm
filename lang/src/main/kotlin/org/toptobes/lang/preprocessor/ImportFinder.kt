package org.toptobes.lang.preprocessor

import org.toptobes.lang.parsing.identifierRegex
import org.toptobes.parsercombinator.ParsingException
import java.io.File

data class Import(val from: File, val symbolNames: List<String>, val mode: ImportMode)

fun findImports(str: String): Pair<String, Set<Import>> {
    val imports = mutableSetOf<Import>()

    val newStr = Regex("import\\s+\"(.*?)\"\\s+(=)?((?:\\s+[!|]\\s+$identifierRegex)*)")
        .replace(str) {
            val file = File(it.groupValues[1])

            if (!file.exists()) {
                throw ParsingException("Can not resolve file ${file.absolutePath}")
            }

            val isEqualsPresent = it.groupValues[2] == "="
            val symbols = it.groupValues[3]

            if (isEqualsPresent == symbols.isBlank()) {
                throw ParsingException("The presence of \"=\" and explicit importation doesn't match")
            }

            if ("!" in symbols && "|" in symbols) {
                throw ParsingException("Can't show and hide symbols @ the same time")
            }

            val mode = when {
                symbols.isBlank() -> All
                "!" in symbols -> Only
                else -> Hiding
            }

            val symbolNames = symbols.split('!', '|').map(String::trim).filter(String::isNotBlank)

            imports += Import(file, symbolNames, mode)
            ""
        }

    return newStr to imports
}

sealed interface ImportMode
object Hiding : ImportMode
object Only : ImportMode
object All : ImportMode
