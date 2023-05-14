package org.toptobes.lang.utils

typealias Word = Short
typealias UWord = UShort

typealias WordArray = ShortArray

fun wordArrayOf(vararg elements: Word) = shortArrayOf(*elements)

fun Collection<Word>.toWordArray() = toShortArray()

fun ByteArray.toWord(): Word {
    require(this.size == 2) { "byte[] is not of size 2" }
    val high = (this[0].toInt() shl 8).toWord()
    val low  = this[1].toWord()
    return (high + low).toWord()
}

fun Number.toBytes(): ByteArray {
    val high = (this.toInt() shr 8).toByte()
    val low  = this.toByte()
    return byteArrayOf(high, low)
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
