package dev.kuro9.application.batch.discord.exception

import kotlinx.io.IOException

class NetworkIOException(
    override val message: String = "Network IO Exception",
    override val cause: Throwable? = null,
) : IOException(message, cause)