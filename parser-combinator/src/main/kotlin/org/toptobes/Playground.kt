package org.toptobes

import org.toptobes.parsercombinator.*
import org.toptobes.parsercombinator.impls.*

fun play() {
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
