package org.toptobes.lang2.nodes

interface Node

object NodeToDelete : Node

sealed interface Identifiable {
    val identifier: String
}

interface Identifiables {
    val varDefs:   Map<String, VariableDefinition>
    val varUsages: Map<String, VariableUsage>
    val typeDefs:  Map<String, TypeDefinition>
}

class MutIdentifiables : Identifiables {
    override val varDefs   = mutableMapOf<String, VariableDefinition>()
    override val varUsages = mutableMapOf<String, VariableUsage>()
    override val typeDefs  = mutableMapOf<String, TypeDefinition>()
}

operator fun <E : Identifiable> MutableMap<String, in E>.plusAssign(new: E) {
    this[new.identifier] = new
}
