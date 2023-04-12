package org.toptobes.vm

import java.nio.ByteBuffer

typealias Memory = ByteBuffer

typealias Word = Short

fun makeMemory(capacity: Int): ByteBuffer {
    return ByteBuffer.allocate(capacity)
}
