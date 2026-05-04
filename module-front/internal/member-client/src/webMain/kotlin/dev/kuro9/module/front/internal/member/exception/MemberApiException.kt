package dev.kuro9.module.front.internal.member.exception

open class MemberApiException(
    val code: Int,
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause) {

    class Unauthorized(override val cause: Throwable? = null) : MemberApiException(
        code = 401,
        message = "Please Login to access this resource.",
        cause = cause,
    )

    class Forbidden(override val cause: Throwable? = null) : MemberApiException(
        code = 403,
        message = "You don't have permission to access this resource.",
        cause = cause,
    )

    class NotFound(override val cause: Throwable? = null) : MemberApiException(
        code = 404,
        message = "Resource not found.",
        cause = cause,
    )
}