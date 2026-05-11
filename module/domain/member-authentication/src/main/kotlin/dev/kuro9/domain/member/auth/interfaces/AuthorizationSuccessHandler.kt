package dev.kuro9.domain.member.auth.interfaces

/**
 * 최초 로그인 성공 및 토큰 리프레시 시 호출
 */
interface AuthorizationSuccessHandler {
    fun onSuccess(userId: Long)
}