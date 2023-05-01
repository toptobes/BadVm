@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.lang.utils.ParsingException
import org.toptobes.parsercombinator.Parser

fun crash(msg: String) = Parser<Nothing> {
    throw ParsingException(msg)
}
