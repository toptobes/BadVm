@file:Suppress("SpellCheckingInspection")

package org.toptobes.lang

import org.toptobes.lang.ast.Symbol
import org.toptobes.lang.codegen.encode
import org.toptobes.lang.parsing.codeParser
import org.toptobes.lang.preprocessor.*
import org.toptobes.lang.utils.prettyString
import org.toptobes.parsercombinator.isOkay
import java.io.File

const val DATA_SEGMENT_START_OFFSET = 2

fun compile(files: List<File>): List<Byte>? {
    val dehydratedSourceFiles = files.map {
        val (code, imports) = findImports(it.readText())
        SourceFile(it, code, imports, emptySet())
    }

    val dependencyGraph = buildDependencyGraph(dehydratedSourceFiles)
    println(dependencyGraph.map { it.sfile.file to it.dependants.map { it.sfile.file } })

    val compilationOrder = sortTopological(dependencyGraph).toMutableList()
    println(compilationOrder.map { it.sfile.file to it.dependants.map { it.sfile.file } })

    val hydratedSourceFiles = mutableSetOf<SourceFile>()

    return compilationOrder.flatMap { node ->
        val imports = resolveImports(node.sfile, hydratedSourceFiles)
        val (bytes, exports) = compile(node.sfile, imports) ?: return null
        hydratedSourceFiles += node.sfile.copy(exports = exports)
        bytes
    }
}

fun resolveImports(sfile: SourceFile, sfiles: Set<SourceFile>): Set<Symbol> = sfile.imports.flatMap { import ->
    val dependency = sfiles.first {
        it.file == import.from
    }

    when (import.mode) {
        All -> {
            dependency.exports
        }
        Hiding -> {
            dependency.exports.filter { it.name !in import.symbolNames }
        }
        Only -> {
            dependency.exports.filter { it.name in import.symbolNames }
        }
    }
}.toSet()

private fun sortTopological(nodes: List<DependencyNode>): List<DependencyNode> {
    val nodesBackup = nodes.map { it.copy(dependants = it.dependants.toMutableList()) }
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

    sorted.forEach { it.dependants.addAll(nodesBackup.first { n -> it.sfile == n.sfile }.dependants) }
    return sorted
}

data class DependencyNode(
    val sfile: SourceFile,
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

data class SourceFile(val file: File, val code: String, val imports: Set<Import>, val exports: Set<Symbol>)

fun compile(sfile: SourceFile, imports: Set<Symbol>): Pair<List<Byte>, Set<Symbol>>? {
    val (newStr, macros) = replaceMacros(sfile.code, imports)
    val labels = findLabels(newStr)

    val symbolMap = (imports + macros + labels).associateBy { it.name }
    val ast = codeParser(newStr, symbolMap)

    return if (ast.isOkay()) {
        println(ast.prettyString())
        val exports = ast.symbols.values.filter { it.export }.toSet()
        encode(ast.result, ast.symbols, ast.allocations) to exports
    } else {
        println(ast.error)
        null
    }
}
