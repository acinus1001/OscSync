package dev.kuro9.common.exception

class DuplicatedInsertException(
    override val message: String = "Duplicated insert",
    override val cause: Throwable? = null,
) : IllegalStateException(message, cause)