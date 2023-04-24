package org.toptobes.lang.nodes

import org.toptobes.lang.utils.Word
import kotlin.properties.Delegates

// -- INTERFACES --

interface VariableUsage : Identifiable

// -- IMPLS --

data class WordVariable(override val identifier: String) : VariableUsage, WordOperand {
    override var value by Delegates.notNull<Word>()
}

data class ByteVariable(override val identifier: String) : VariableUsage, ByteOperand {
    override var value by Delegates.notNull<Byte>()
}

data class AddrVariable(override val identifier: String) : VariableUsage, AddrOperand {
    override var address by Delegates.notNull<Word>()
}

data class Label(override val identifier: String) : VariableUsage, AddrOperand {
    override var address by Delegates.notNull<Word>()
}
