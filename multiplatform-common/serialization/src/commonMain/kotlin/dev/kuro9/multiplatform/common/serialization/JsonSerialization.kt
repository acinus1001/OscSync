@file:OptIn(ExperimentalSerializationApi::class)

package dev.kuro9.multiplatform.common.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

val prettyJson = Json {
    prettyPrint = true
    allowTrailingComma = true
    decodeEnumsCaseInsensitive = true
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
    allowComments = true
}

val minifyJson = Json {
    prettyPrint = false
    allowTrailingComma = true
    decodeEnumsCaseInsensitive = true
    encodeDefaults = true
    explicitNulls = false
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
    allowComments = true
}