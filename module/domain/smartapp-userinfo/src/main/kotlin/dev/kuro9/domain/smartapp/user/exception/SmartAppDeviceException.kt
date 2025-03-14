package dev.kuro9.domain.smartapp.user.exception

open class SmartAppDeviceException : RuntimeException() {

    class DuplicatedRegisterException(
        override val message: String = "Duplicated register",
        override val cause: Throwable? = null
    ) : SmartAppDeviceException()

    class NotSupportException(
        override val message: String = "Not supported",
        override val cause: Throwable? = null
    ) : SmartAppDeviceException()

    class NotFoundException(
        override val message: String = "Device Not Exists",
        override val cause: Throwable? = null
    ) : SmartAppDeviceException()
}