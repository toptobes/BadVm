package org.toptobes.lang.parsing

import org.toptobes.parsercombinator.impls.regex
import org.toptobes.parsercombinator.impls.sequence
import org.toptobes.parsercombinator.impls.withDefault

val identifier = sequence(
    regex("[_a-zA-Z]"),
    regex("\\w+") withDefault "",
).map { it[0] + it[1] }
