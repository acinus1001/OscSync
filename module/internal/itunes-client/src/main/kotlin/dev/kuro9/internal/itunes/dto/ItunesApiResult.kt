package dev.kuro9.internal.itunes.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItunesApiResult<T>(
    val resultCount: Int,
    val results: List<T>,
)
