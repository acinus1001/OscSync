package dev.kuro9.domain.member.auth.handler

import dev.kuro9.domain.member.auth.config.CookieConfigProperties
import dev.kuro9.domain.member.auth.interfaces.AuthorizationSuccessHandler
import dev.kuro9.domain.member.auth.jwt.JwtTokenService
import dev.kuro9.domain.member.auth.service.DiscordOAuth2TokenManageService
import dev.kuro9.multiplatform.common.date.util.toLocalDateTime
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinInstant

@[Component Order(Int.MAX_VALUE)]
class OAuth2SuccessHandler(
    private val tokenService: JwtTokenService,
    private val cookieProperties: CookieConfigProperties,
    private val authorizedClientService: OAuth2AuthorizedClientService,
    private val authorizationSuccessHandlerList: List<AuthorizationSuccessHandler>,
    private val discordTokenService: DiscordOAuth2TokenManageService,
) : AuthenticationSuccessHandler {

    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val userId = tokenService.getUserId(authentication)
        run {
            if (authentication !is OAuth2AuthenticationToken) return@run

            // 2. 현재 로그인한 클라이언트 정보(디스코드, 구글 등)와 유저 이름(ID) 추출
            val clientRegistrationId = authentication.authorizedClientRegistrationId
            val principalName = authentication.name // 유저의 고유 식별자 ID

            // 3. AuthorizedClient를 통해 OAuth2 토큰들이 담긴 객체 로드
            val authorizedClient: OAuth2AuthorizedClient? = authorizedClientService.loadAuthorizedClient(
                clientRegistrationId,
                principalName
            )

            if (authorizedClient != null) {
                val accessTokenValue = authorizedClient.accessToken.tokenValue
                val expiresAt = authorizedClient.accessToken.expiresAt!!.toKotlinInstant().toLocalDateTime()
                val refreshTokenValue = authorizedClient.refreshToken?.tokenValue

//                info { "Access Token: $accessTokenValue, Refresh Token: $refreshTokenValue, expiresAt: $expiresAt" }

                runBlocking {
                    discordTokenService.saveToken(
                        userId = userId,
                        accessToken = accessTokenValue,
                        refreshToken = refreshTokenValue!!,
                        expiresAt = expiresAt
                    )
                }
            }
        }

        authorizationSuccessHandlerList.forEach { it.onSuccess(userId) }
        val tokenResponse = tokenService.makeTokenResponse(authentication)

        val accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken)
            .httpOnly(true)
            .secure(cookieProperties.secure)
            .domain(cookieProperties.domain)
            .sameSite("Lax")
            .path("/")
            .maxAge(30.minutes.toJavaDuration())
            .build()

        val refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken)
            .httpOnly(true)
            .secure(cookieProperties.secure)
            .domain(cookieProperties.domain)
            .sameSite("Lax")
            .path("/")
            .maxAge(7.days.toJavaDuration())
            .build()

        response.run {
            addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
            addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
            sendRedirect(cookieProperties.redirectFrontUri)
        }
    }
}