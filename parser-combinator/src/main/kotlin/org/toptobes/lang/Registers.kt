package org.toptobes.lang

val reg16Codes = "sp, bp, ax, bx, cx, dx"
    .split(", ")
    .mapIndexed { idx, name -> name to (idx + 1).toByte() }
    .toMap()

val reg8Codes = "ah, al, bh, bl, ch, cl, dh, dl"
    .split(", ")
    .mapIndexed { idx, name -> name to (idx).toByte() }
    .toMap()
