package dev.kuro9.application.homepage.controller

import dev.kuro9.application.homepage.repository.HomepageAuthResourceEntity
import dev.kuro9.application.homepage.repository.HomepageAuthResources
import dev.kuro9.domain.member.auth.model.DiscordUserDetail
import org.jetbrains.exposed.v1.core.eq
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaDuration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@RestController
@Transactional(readOnly = true)
@RequestMapping("/resources")
@OptIn(ExperimentalUuidApi::class)
class AuthResourceController {

    @GetMapping("/strings/{id}")
    fun getString(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable id: String,
    ): ResponseEntity<String> {
        val text = he.find(h.externalId eq Uuid.parse(id))
            .singleOrNull()
            ?.takeIf { it.type == MediaType.TEXT_PLAIN_VALUE }
            ?.takeIf {
                val requireAuthorities = it.allowed
                user.hasAnyPermissionOf(*requireAuthorities.toTypedArray())
            }
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(text.resource.bytes.toString(Charsets.UTF_8))
    }

    @GetMapping("/images/{id}")
    fun getImage(
        @AuthenticationPrincipal user: DiscordUserDetail,
        @PathVariable id: String,
    ): ResponseEntity<ByteArray> {
        val image = he.find(h.externalId eq Uuid.parse(id))
            .singleOrNull()
            ?.takeIf { it.type.startsWith("image/") }
            ?.takeIf {
                val requireAuthorities = it.allowed
                user.hasAnyPermissionOf(*requireAuthorities.toTypedArray())
            }
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(7.days.toJavaDuration()))
            .contentType(MediaType.parseMediaType(image.type))
            .body(image.resource.bytes)
    }

    private typealias h = HomepageAuthResources
    private typealias he = HomepageAuthResourceEntity
}