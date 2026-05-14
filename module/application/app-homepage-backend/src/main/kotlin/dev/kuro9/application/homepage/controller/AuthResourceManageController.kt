package dev.kuro9.application.homepage.controller

import dev.kuro9.application.homepage.repository.HomepageAuthResourceEntity
import dev.kuro9.application.homepage.repository.HomepageAuthResources
import dev.kuro9.multiplatform.common.date.util.now
import dev.kuro9.multiplatform.common.types.app.homepage.common.*
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@RestController
@Transactional
@RequestMapping("/resources/admin")
@OptIn(ExperimentalUuidApi::class)
class AuthResourceManageController {

    @GetMapping("/strings")
    fun getStringList(): StringResourceListResponse {
        return he.find { h.type eq MediaType.TEXT_PLAIN_VALUE }.map {
            StringResourceListResponse.Element(
                externalId = it.externalId,
                description = it.description,
                allowed = it.allowed,
                string = it.resource.bytes.toString(Charsets.UTF_8),
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }.let(::StringResourceListResponse)
    }

    @GetMapping("/strings/{id}")
    fun getStringInfo(
        @PathVariable id: String,
    ): ResponseEntity<StringResourceResponse> {
        val data = he.find(h.externalId eq Uuid.parse(id))
            .singleOrNull()
            ?: return ResponseEntity.notFound().build()

        return StringResourceResponse(
            description = data.description,
            allowed = data.allowed,
            string = data.resource.bytes.toString(Charsets.UTF_8),
            createdAt = data.createdAt,
            updatedAt = data.updatedAt,
        ).let(ResponseEntity.ok()::body)
    }

    @PostMapping("/strings")
    fun postString(@RequestBody body: StringResourcePostRequest): ResponseEntity<String> {
        val id = h.insertReturning {
            it[h.description] = body.description
            it[h.type] = MediaType.TEXT_PLAIN_VALUE
            it[h.allowed] = body.allowed
            it[h.resource] = body.string.toByteArray(Charsets.UTF_8).let(::ExposedBlob)
        }.single()[h.externalId]
        return ResponseEntity.ok(id.toHexString())
    }

    @PatchMapping("/strings/{id}")
    fun modifyString(
        @RequestBody body: StringResourceModifyRequest,
        @PathVariable id: String,
    ): ResponseEntity<Nothing> {
        val updated = h.update(where = { h.externalId eq Uuid.parse(id) }) {
            if (body.description != null) it[h.description] = body.description!!
            if (body.allowed != null) it[h.allowed] = body.allowed!!
            if (body.string != null) it[h.resource] = body.string!!.toByteArray(Charsets.UTF_8).let(::ExposedBlob)

            it[h.updatedAt] = LocalDateTime.now()
        }

        return when (updated) {
            1 -> ResponseEntity.ok(null)
            else -> ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/images")
    fun getImageList(): ImageResourceListResponse {
        return he.find { h.type like "image/%" }.map {
            ImageResourceListResponse.Element(
                externalId = it.externalId,
                description = it.description,
                allowed = it.allowed,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }.let(::ImageResourceListResponse)
    }

    @GetMapping("/images/{id}")
    fun getImage(@PathVariable id: String): ResponseEntity<ImageResourceResponse> {
        val image = he.find(h.externalId eq Uuid.parse(id))
            .singleOrNull()
            ?: return ResponseEntity.notFound().build()

        return ImageResourceResponse(
            externalId = image.externalId,
            description = image.description,
            allowed = image.allowed,
            createdAt = image.createdAt,
            updatedAt = image.updatedAt,
        ).let(ResponseEntity.ok()::body)
    }

    @PostMapping("/images")
    fun postImage(
        @RequestPart("image") image: MultipartFile,
        @RequestPart("body") body: ImageResourcePostRequest,
    ): ResponseEntity<String> {

        val id = h.insertReturning {
            it[h.description] = body.description
            it[h.type] = image.contentType!!
            it[h.allowed] = body.allowed
            it[h.resource] = ExposedBlob(image.bytes)
        }.single()[h.externalId]
        return ResponseEntity.ok(id.toHexString())
    }

    @PatchMapping("/images/{id}")
    fun modifyImage(
        @PathVariable id: String,
        @RequestBody body: ImageResourceModifyRequest,
    ): ResponseEntity<Nothing> {
        val updated = h.update(where = { h.externalId eq Uuid.parse(id) }) {
            if (body.description != null) it[h.description] = body.description!!
            if (body.allowed != null) it[h.allowed] = body.allowed!!

            it[h.updatedAt] = LocalDateTime.now()
        }

        return when (updated) {
            1 -> ResponseEntity.ok(null)
            else -> ResponseEntity.notFound().build()
        }
    }

    private typealias h = HomepageAuthResources
    private typealias he = HomepageAuthResourceEntity
}