@file:Suppress("ClassName", "MemberVisibilityCanBePrivate")

package org.toptobes.parsercombinator.impls

import org.toptobes.parsercombinator.Parser
import org.toptobes.parsercombinator.success

fun <R> succeed(value: R) = Parser { oldState ->
    success(oldState, value)
}
