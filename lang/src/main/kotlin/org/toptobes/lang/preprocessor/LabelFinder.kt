package org.toptobes.lang.preprocessor

import org.toptobes.RESERVED_MEM_SIZE
import org.toptobes.lang.ast.Label
import org.toptobes.parsercombinator.VarMap

fun findLabels(str: String): VarMap {
    val labels = Regex("(^|[^({,]\\s+)([_a-zA-Z]\\w*):")
        .findAll(str)
        .map { Label(it.groupValues[2]) }
        .associateBy { it.name }

    return VarMap(labels, RESERVED_MEM_SIZE)
}
