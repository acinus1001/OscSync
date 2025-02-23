package dev.kuro9.internal.smartapp.model.response

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmartAppDeviceListResponse(
    val items: List<DeviceInfo>,
    @SerialName("_links") override val links: SmartAppResponse.Paged.LinkInfo
) : SmartAppResponse.Paged {

    @Serializable
    data class DeviceInfo(
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