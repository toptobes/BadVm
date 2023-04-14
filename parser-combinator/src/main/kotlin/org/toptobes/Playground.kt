package org.toptobes

import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun playground1() {
    val p1 = sequence(str("abc"), any(str("def"))).map { it.joinToString("") }

    val p2 = regex("[a-z]+")

    val p3 = +str("abc")

    val p4 = str("abc") then
            whitespace then
            sepBy.commas(digits)..{ it.joinToString("|") }..{ "[$it]" }

//    p4.log("abc 1,2,3")

    lateinit var p5: Parser<String, String>
    val choices = lazy {
        any(p5, between(!whitespace, letters))
    }
    p5 = between.squareBrackets(sepBy.whitespaceInsensitiveCommas(choices)).map { it.toString() }

//    p5.log("[a, b, [c]]")
}

fun playground2() {
    val str1 = "string    a='str'    "
    val str2 = "integer  b = 3  "
    val str3 = " boolean c = true"

    val string = (str("'") then letters then str("'"))..{ it.joinToString("") }
    val integer = digits
    val boolean = any(str("true"), str("false"))

    val dec = contextual(str3) { ctx ->
        val type = ctx parse -any(
            str("string"),
            str("integer"),
            str("boolean"),
        )

        val name = ctx.parse(-letters)

        ctx.parse(-str("="))

        val data = ctx parse -when (type) {
            "string"  -> string
            "integer" -> integer
            else -> boolean
        }

        "val $name = $data"
    }

    println(dec)
}
