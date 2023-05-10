package org.toptobes.lang.utils

typealias Word = Short
typealias UWord = UShort

fun List<Byte>.toWord(): Short {
    require(this.size == 2) { "List of bytes is not of size 2" }
    val high = (this[0].toInt() shl 8).toWord()
    val low  = this[1].toWord()
    return (high + low).toWord()
}

fun Word.toBytes(): List<Byte> {
    val high = (this.toInt() shr 8).toByte()
    val low  = this.toByte()
    return listOf(high, low)
}

fun Int.toWord() = toShort()
fun Int.toUWord() = toUShort()

fun Byte.toWord() = toShort()
fun Byte.toUWord() = toUShort()

fun String.toWord() = toShort()
fun String.toUWord() = toUShort()

fun String.toWordOrNull() = toShortOrNull()
fun String.toUWordOrNull() = toUShortOrNull()
