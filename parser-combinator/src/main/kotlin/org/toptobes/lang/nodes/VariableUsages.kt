package org.toptobes.lang.nodes

import org.toptobes.lang.utils.Word
import kotlin.properties.Delegates

// -- INTERFACES --

sealed interface VariableUsage : Identifiable

sealed interface DehydratedVarUsage : VariableUsage
sealed interface HydratedVarUsage   : VariableUsage

// -- IMPLS --

data class WordVariable(
    override val identifier: String,
    override val value: Word
) : HydratedVarUsage, WordOperand

data class ByteVariable(
    override val identifier: String,
    override val value: Byte
) : HydratedVarUsage, ByteOperand

data class AddrHolderVariable(
    override val identifier: String,
    val to: StaticDefinition
) : DehydratedVarUsage, WordOperand {
    override var value by Delegates.notNull<Word>()
}

data class ByteAddrVariable(
    override val identifier: String,
    val to: StaticDefinition
) : DehydratedVarUsage, WordAddrOperand {
    override var address by Delegates.notNull<Word>()
}

data class WordAddrVariable(
    override val identifier: String,
    val to: StaticDefinition
) : DehydratedVarUsage, ByteAddrOperand {
    override var address by Delegates.notNull<Word>()
}

data class Label(override val identifier: String) : DehydratedVarUsage, WordAddrOperand {
    override var address by Delegates.notNull<Word>()
}

data class EmbeddedBytesVariable(
    override val identifier: String,
    val originalDef: StaticDefinition,
    val value: List<Byte>
) : HydratedVarUsage
