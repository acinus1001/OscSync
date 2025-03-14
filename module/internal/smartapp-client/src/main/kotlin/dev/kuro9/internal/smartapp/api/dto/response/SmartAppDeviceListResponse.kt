package dev.kuro9.internal.smartapp.api.dto.response

import dev.kuro9.internal.smartapp.api.dto.response.SmartAppResponseObject.DeviceInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmartAppDeviceListResponse(
    val items: List<DeviceInfo>,
    @SerialName("_links") override val links: SmartAppResponse.Paged.LinkInfo
) : SmartAppResponse.Paged