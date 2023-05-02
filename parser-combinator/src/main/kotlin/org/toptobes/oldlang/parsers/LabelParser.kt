package org.toptobes.oldlang.parsers

import org.toptobes.oldlang.nodes.LabelDefinition
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.impls.strOf

val labelDefinition = strOf(identifier, str(':'))
    .map { it.dropLast(1) }
    .map(::LabelDefinition)
