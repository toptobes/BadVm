package org.toptobes

import org.toptobes.lang.compile
import org.toptobes.parsercombinator.DescriptiveParsingException
import java.io.File

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
