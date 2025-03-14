package dev.kuro9.internal.smartapp.api.dto.response

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

interface SmartAppResponseObject {

    @Serializable
    open class DeviceInfo(
        val deviceId: String,
        val name: String?,
        val label: String?,
        val manufacturerName: String,
        val presentationId: String,

        val locationId: String?,
        val ownerId: String?,
        val roomId: String?,

        val components: List<Component>,

        val createTime: Instant,

        val parentDeviceId: String?,
    ) {

        @Serializable
        data class Component(
            val id: String,
            val capabilities: List<Capability>,
            val categories: List<Category>,
            val label: String?,
        ) {

            @Serializable
            data class Capability(
                val id: String,
                val version: Int?,
            )

            @Serializable
            data class Category(
                val name: String,
            )
        }
    }
}