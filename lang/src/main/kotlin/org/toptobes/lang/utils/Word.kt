package org.toptobes.lang.utils

import kotlin.experimental.or

typealias Word = Short
typealias UWord = UShort

typealias WordArray = ShortArray

fun wordArrayOf(vararg elements: Word) = shortArrayOf(*elements)

fun Collection<Word>.toWordArray() = toShortArray()

fun ByteArray.toWord(): Word {
    require(this.size == 2) { "byte[] is not of size 2" }
    val high = (this[0].toInt() shl 8).toUWord().toInt()
    val low  = this[1].toUByte().toInt()
    return (high + low).toWord()
}

fun Byte.toBytes(): ByteArray {
    return byteArrayOf(this)
}

fun Byte.toBytesList(): List<Byte> {
    return listOf(this)
}

fun Number.toBytes(): ByteArray {
    val high = (this.toInt() shr 8).toByte()
    val low  = this.toByte()
    return byteArrayOf(high, low)
}

fun Number.toBytesList(): List<Byte> {
    val high = (this.toInt() shr 8).toByte()
    val low  = this.toByte()
    return listOf(high, low)
}

fun WordArray.toBytes() = ByteArray(size * 2).also {
    this.forEachIndexed { i, n ->
        it[i * 2] = (n.toInt() shr 8).toByte()
        it[i * 2 + 1] = n.toByte()
    }
}

fun Number.toWord() = toShort()
fun Int.toUWord() = toUShort()
fun Byte.toUWord() = toUShort()

fun String.toWord() = toShort()
fun String.toUWord() = toUShort()

fun String.toWordOrNull() = toShortOrNull()
fun String.toUWordOrNull() = toUShortOrNull()
