package dev.kuro9.domain.smartapp.user.exception

open class SmartAppUserException : RuntimeException() {

    class CredentialNotFoundException(
        override val message: String = "user token not found",
        override val cause: Throwable? = null
    ) : SmartAppUserException()
}