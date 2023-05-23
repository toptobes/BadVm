@file:Suppress("SpellCheckingInspection")

package org.toptobes.lang

import org.toptobes.lang.ast.Label
import org.toptobes.lang.ast.Symbol
import org.toptobes.lang.codegen.encode
import org.toptobes.lang.parsing.codeParser
import org.toptobes.lang.preprocessor.*
import org.toptobes.lang.utils.Word
import org.toptobes.lang.utils.prettyString
import org.toptobes.lang.utils.toBytesList
import org.toptobes.lang.utils.toWord
import org.toptobes.parsercombinator.isOkay
import java.io.File

const val DATA_SEGMENT_START_OFFSET = 2

fun compile(files: List<File>): List<Byte>? {
    val dehydratedSourceFiles = files.map {
        val code1 = resolveIncludes(it.readText(), it)
        val (code2, imports) = findImports(code1, it)
        SourceFile(it.canonicalPath, code2, imports, emptySet())
    }

    val dependencyGraph = buildDependencyGraph(dehydratedSourceFiles)

    val compilationOrder = sortTopological(dependencyGraph).toMutableList()

    val hydratedSourceFiles = mutableSetOf<SourceFile>()

    val bytes = compilationOrder.map { node ->
        val imports = resolveImports(node.sfile, hydratedSourceFiles)
        val (segments, exports) = compile(node.sfile, imports) ?: return null
        hydratedSourceFiles += node.sfile.copy(exports = exports)
        segments
    }

    val startLabel = hydratedSourceFiles.flatMap { it.exports }.find { it.name == "_start" } as? Label
    val startAddr = startLabel?.address ?: 0

    return startAddr.toBytesList() + bytes
}

private data class DependencyNode(
    val sfile: SourceFile,
    val dependants: MutableList<DependencyNode>,
)

private data class SourceFile(
    val fp: String,
    val code: String,
    val imports: Set<Import>,
    val exports: Set<Symbol>
)

private fun resolveImports(sfile: SourceFile, sfiles: Set<SourceFile>): Set<Symbol> = sfile.imports.flatMap { import ->
    val dependency = sfiles.first {
        it.fp == import.fp
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
    return sorted.reversed()
}

private fun buildDependencyGraph(files: List<SourceFile>): List<DependencyNode> {
    val fileMap = files.associateBy { it.fp }
    val dependencyMap = mutableMapOf<String, DependencyNode>()

    fun buildNode(fp: String): DependencyNode {
        val sourceFile = fileMap[fp]!!

        val dependants = files.flatMap { currentFile ->
            currentFile.imports.mapNotNull { import ->
                if (import.fp == fp) {
                    dependencyMap.getOrPut(currentFile.fp) { buildNode(currentFile.fp) }
                } else null
            }
        }.toMutableList()

        return DependencyNode(sourceFile, dependants)
    }

    return files.map { file ->
        dependencyMap.getOrPut(file.fp) { buildNode(file.fp) }
    }
}

private fun compile(sfile: SourceFile, imports: Set<Symbol>): Pair<Segments, Set<Symbol>>? {
    val (newStr, macros) = replaceMacros(sfile.code, imports)
    val labels = findLabels(newStr)

    val symbolMap = (imports + macros + labels).associateBy { it.name }
    val parseResult = codeParser(newStr, symbolMap)

    return if (parseResult.isOkay()) {
//        println(parseResult.prettyString())
        val exports = parseResult.symbols.values.filter { it.export || it.name == "_start" }.toSet()
        val (dataSeg, instructions) = encode(parseResult, instructionsStart + parseResult.allocations.size)
        Triple(dataSeg, instructions, exports)
    } else {
//        println(parseResult.error)
        null
    }
}
