package org.toptobes.lang.utils

infix fun <T, R> Iterable<T>.zipMap(other: Iterable<R>): Map<T, R> {
    return (this zip other).toMap()
}
