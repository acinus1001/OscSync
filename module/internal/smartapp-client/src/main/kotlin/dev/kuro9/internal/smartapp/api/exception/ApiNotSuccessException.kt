package dev.kuro9.internal.smartapp.api.exception

class ApiNotSuccessException(
    val code: Int,
    val httpMessage: String,
    override val cause: Throwable?
) :
    IllegalStateException(
        "Http Call Failed : code=$code, message=$httpMessage"
    )