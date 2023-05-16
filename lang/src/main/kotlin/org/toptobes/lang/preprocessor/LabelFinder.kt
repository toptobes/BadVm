package org.toptobes.lang.preprocessor

import org.toptobes.lang.ast.Label
import org.toptobes.parsercombinator.SymbolMap

fun findLabels(str: String): SymbolMap {
    val labels = Regex("(^|[^({,]\\s+)([_a-zA-Z]\\w*):")
        .findAll(str)
        .map { Label(it.groupValues[2], false) }
        .associateBy { it.name }

    return labels
}
