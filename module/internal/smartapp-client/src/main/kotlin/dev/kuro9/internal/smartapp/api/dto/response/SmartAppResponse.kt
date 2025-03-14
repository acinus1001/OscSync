package dev.kuro9.internal.smartapp.api.dto.response

import dev.kuro9.internal.smartapp.api.response.SmartAppPagedLinkInfoSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface SmartAppResponse {

    @Serializable
    data class SimpleResult(
        val results: List<ResultInfo>,
    ) : SmartAppResponse {
        @Serializable
        data class ResultInfo(
            val id: String,
            val status: String,
        )
    }

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