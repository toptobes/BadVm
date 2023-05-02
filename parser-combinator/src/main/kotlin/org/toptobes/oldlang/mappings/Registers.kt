package org.toptobes.oldlang.mappings

val reg16Codes = "sp, bp, ax, bx, cx, dx"
    .split(", ")
    .mapIndexed { idx, name -> name to (idx + 1).toByte() }
    .toMap()

val reg8Codes = "ah, al, bh, bl, ch, cl, dh, dl"
    .split(", ")
    .mapIndexed { idx, name -> name to ((idx + 1) * 2).toByte() }
    .toMap()
