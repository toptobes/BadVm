package org.toptobes

import org.toptobes.lang.compile
import org.toptobes.parsercombinator.DescriptiveParsingException
import java.io.File

/**
 * ```
 * imm/mem:
 * 000000000 0 00000 0 00000000 00000000
 *           ^ ^^^^^ ^ ^^^^^^^^ ^^^^^^^^
 *           a   r   l     b        b
 *           d   e   e     y        y
 *           j   f   n     t        t
 *           u       g     e        e
 *           s   i   t
 *           t   d   h     h        l
 */
fun main() {
    try {
        val files = listOf(
            "../examples/Main.bsm",
            "../examples/Graph.bsm",
        ).map(::File)

        val code = compile(files)
        println(code)

        File("../out").bufferedWriter().use { writer ->
            code?.let { writer.write(it.joinToString(",")) }
        }
    } catch (e: DescriptiveParsingException) {
        println(e.message)
    }
}
