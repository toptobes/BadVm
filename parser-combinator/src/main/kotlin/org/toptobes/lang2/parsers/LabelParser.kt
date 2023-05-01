package org.toptobes.lang2.parsers

import org.toptobes.lang2.nodes.LabelDefinition
import org.toptobes.parsercombinator.impls.str
import org.toptobes.parsercombinator.impls.strOf

val labelDefinition = strOf(identifier, str(':'))
    .map { it.dropLast(1) }
    .map(::LabelDefinition)
