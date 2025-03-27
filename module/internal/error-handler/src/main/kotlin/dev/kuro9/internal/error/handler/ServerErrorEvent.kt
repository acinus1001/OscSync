package dev.kuro9.internal.error.handler

data class ServerErrorEvent(
    val t: Throwable,
)
