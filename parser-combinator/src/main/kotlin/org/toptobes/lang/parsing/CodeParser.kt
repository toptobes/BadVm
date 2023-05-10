package org.toptobes.lang.parsing

import org.toptobes.parsercombinator.impls.any
import org.toptobes.parsercombinator.unaryMinus
import org.toptobes.parsercombinator.unaryPlus

val codeParser = +-any(instructionsParser, typeDefinition, variableDefinition)
