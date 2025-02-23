package dev.kuro9.internal.smartapp.model.response

import dev.kuro9.internal.smartapp.serializer.response.SmartAppPagedLinkInfoSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface SmartAppResponse {

    @Serializable
    sealed interface Paged : SmartAppResponse {
        @SerialName("_links")
        val links: LinkInfo

        @Serializable(with = SmartAppPagedLinkInfoSerializer::class)
        data class LinkInfo(
            val next: String?,
            val previous: String?,
        )
    }
}