package dev.kuro9.domain.member.auth.jwt

@FunctionalInterface
interface JwtTokenValidator : (JwtBasicPayload) -> Boolean {

    fun validate(payload: JwtBasicPayload): Boolean
    override fun invoke(payload: JwtBasicPayload): Boolean = validate(payload)
}