package dev.kuro9.internal.error.handler

interface ServerErrorHandler {
    suspend fun doHandle(t: Throwable)
}