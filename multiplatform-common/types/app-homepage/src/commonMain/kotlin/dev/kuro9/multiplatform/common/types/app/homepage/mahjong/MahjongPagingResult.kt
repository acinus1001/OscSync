package dev.kuro9.multiplatform.common.types.app.homepage.mahjong

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class MahjongPagingResult<T : Any>(
    @ProtoNumber(1) val page: Int,
    @ProtoNumber(2) val maxPage: Int,
    @ProtoNumber(3) val totalElementCount: Int,
    @ProtoNumber(4) val content: List<T>,
)