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

data class AddrVariable(override val identifier: String) : DehydratedVarUsage, AddrOperand {
    override var address by Delegates.notNull<Word>()
}

data class Label(override val identifier: String) : DehydratedVarUsage, AddrOperand {
    override var address by Delegates.notNull<Word>()
}

data class EmbeddedBytesVariable(
    override val identifier: String,
    val value: List<Byte>
) : HydratedVarUsage
