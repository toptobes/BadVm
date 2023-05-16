package org.toptobes.lang.preprocessor

import org.toptobes.lang.ast.Label
import org.toptobes.lang.parsing.identifierRegex
import org.toptobes.parsercombinator.SymbolMap

/**
 * Something is a label if it matches the format `<name>:`
 * and doesn't have a comma, {, or ( right before it.
 * It also requires some amount of whitespace before it, or
 * the start of the file.
 */
fun findLabels(str: String): SymbolMap =
    Regex("(^|[^({,]\\s+)($identifierRegex):")
        .findAll(str)
        .map { Label(it.groupValues[2], false) }
        .associateBy { it.name }
