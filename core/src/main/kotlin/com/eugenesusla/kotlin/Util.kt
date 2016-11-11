package com.eugenesusla.kotlin

internal inline fun <R> tryOrNull(f: () -> R): R? {
    try {
        return f()
    } catch (e: Throwable) {
        return null;
    }
}

internal operator fun Int.contains(bit: Int) = (this and bit) != 0

internal fun Any?.toStringWithClass() =
        "${this?.javaClass?.simpleName ?: "Null"}($this)"