@file:OptIn(ExperimentalSerializationApi::class)

package dev.kuro9.multiplatform.common.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf

val protoBuf = ProtoBuf {
    this.encodeDefaults = true
}