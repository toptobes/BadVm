package org.toptobes.lang

import org.toptobes.lang.ast.Symbol
import org.toptobes.lang.codegen.encode
import org.toptobes.lang.parsing.codeParser
import org.toptobes.lang.preprocessor.*
import org.toptobes.lang.utils.prettyString
import org.toptobes.parsercombinator.isOkay
import java.io.File

const val DATA_SEGMENT_START_OFFSET = 2

fun compile(vararg files: File) {
    val dehydratedSourceFiles = files.map {
        val (code, imports) = findImports(it.readText())
        SourceFile(it, code, imports, emptyList())
    }

    val dependencyGraph = buildDependencyGraph(dehydratedSourceFiles)
    val compilationOrder = sortTopological(dependencyGraph)
}

private fun sortTopological(nodes: List<DependencyNode>): List<DependencyNode> {
    val roots = nodes.filter { it.dependants.isEmpty() }.toMutableList()
    val sorted = mutableListOf<DependencyNode>()

    while (roots.isNotEmpty()) {
        val n = roots.removeFirst()
        sorted += n

        for (m in nodes.filter { n in it.dependants }) {
            m.dependants.remove(n)

            if (m.dependants.isEmpty()) {
                roots += m
            }
        }
    }

    return sorted
}

data class DependencyNode(
    val file: SourceFile,
    val dependants: MutableList<DependencyNode>,
)

fun buildDependencyGraph(files: List<SourceFile>): List<DependencyNode> {
    val fileMap = files.associateBy { it.file }
    val dependencyMap = mutableMapOf<File, DependencyNode>()

    fun buildNode(file: File): DependencyNode {
        val sourceFile = fileMap[file]!!

        val dependants = files.flatMap { currentFile ->
            currentFile.imports.mapNotNull { import ->
                if (import.from == file) {
                    dependencyMap.getOrPut(currentFile.file) { buildNode(currentFile.file) }
                } else null
            }
        }.toMutableList()

        return DependencyNode(sourceFile, dependants)
    }

    return files.map { file ->
        dependencyMap.getOrPut(file.file) { buildNode(file.file) }
    }
}

data class SourceFile(val file: File, val code: String, val imports: List<Import>, val exports: List<Symbol>)

fun compile(str: String): List<Byte>? {
    val (newStr, macros) = findMacros(str)
    val (newStr2, imports) = findImports(newStr)
    return null
    val newStr3 = replaceMacros(macros, newStr2)
    val labels = findLabels(newStr3)
    val ast = codeParser(newStr3, labels)

    return if (ast.isOkay()) {
        println(ast.prettyString())
        encode(ast.result, ast.symbols, ast.allocations)
    } else {
        println(ast.error)
        null
    }
}
