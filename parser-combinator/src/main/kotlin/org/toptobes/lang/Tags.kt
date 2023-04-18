package org.toptobes.lang

import org.toptobes.parsercombinator.Parser
import org.toptobes.utils.Either

const val REG16      = "REG16"
const val REG8       = "REG8"
const val IMM16      = "IMM16"
const val IMM8       = "IMM8"
const val VARIABLE   = "VAR"
const val MEM        = "MEM"
const val PTR        = "PTR"
const val LABEL      = "LBL"
const val LABEL_DEF  = "LBL_DEF"
const val DB_DEF     = "DB_DEF"
const val DW_DEF     = "DW_DEF"
const val CONSTB_DEF = "CONSTB_DEF"
const val CONSTW_DEF = "CONSTW_DEF"
const val CONST_ADR  = "CONST_ADR"
const val CONST16    = "CONST16"
const val CONST8     = "CONST8"

@Suppress("IMPLICIT_CAST_TO_ANY")
data class TaggedValue(val tag: String, val value: Either<String, Either<TaggedValue, List<TaggedValue>>>) {
    override fun toString() = """
        { "$tag": ${if (isTaggedString) "\"$valAsString\"" else if (isTaggedStruct) valAsStruct else valAsList } }
    """.trimIndent()
}

val TaggedValue.isTaggedString
    get() = value is Either.Left

val TaggedValue.isTaggedStruct
    get() = (value as? Either.Right)?.value is Either.Left

val TaggedValue.isTaggedList
    get() = (value as? Either.Right)?.value is Either.Right

val TaggedValue.valAsString
    get() = (value as Either.Left).value

val TaggedValue.valAsStruct
    get() = ((value as Either.Right).value as Either.Left).value

val TaggedValue.valAsList
    get() = ((value as Either.Right).value as Either.Right).value

infix fun String.tagString(b: String) = TaggedValue(this, Either.Left(b))

fun <T> Parser<T, String>.tagString(tag: String) =
    this.map { tag tagString it }

infix fun String.tagStruct(b: TaggedValue) = TaggedValue(this, Either.Right(Either.Left(b)))

fun <T> Parser<T, TaggedValue>.tagStruct(tag: String) =
    this.map { tag tagStruct it }

infix fun String.tagList(b: List<TaggedValue>) = TaggedValue(this, Either.Right(Either.Right(b)))

fun <T> Parser<T, List<TaggedValue>>.tagList(tag: String) =
    this.map { tag tagList it }

fun <T> Parser<T, TaggedValue>.flatTag(tag: String) =
    this.map { if (it.isTaggedString) tag tagString it.valAsString else tag tagStruct it.valAsStruct }
