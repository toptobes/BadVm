package org.toptobes.lang.nodes

interface Node

sealed interface Identifiable {
    val identifier: String
}

object NodeToDelete : Node

interface Nodes {
    val varDefs: Map<String, VariableDefinition>
    val varUsages: Map<String, VariableUsage>
    val typeDefs: Map<String, TypeDefinition>
}

class MutNodes : Nodes {
    override val varDefs = mutableMapOf<String, VariableDefinition>()
    override val varUsages = mutableMapOf<String, VariableUsage>()
    override val typeDefs = mutableMapOf<String, TypeDefinition>()
}

operator fun <E : Identifiable> MutableMap<String, in E>.plusAssign(new: E) {
    this[new.identifier] = new
}
