package dev.kuro9.domain.cache.serializer

import dev.kuro9.multiplatform.common.serialization.protoBuf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import org.springframework.data.redis.serializer.RedisSerializer

@OptIn(ExperimentalSerializationApi::class)
class KotlinRedisProtoBufSerializer<T : Any>(private val serializer: KSerializer<T>) : RedisSerializer<T> {

    override fun serialize(value: T?): ByteArray {
        return protoBuf.encodeToByteArray(serializer, value!!)
    }

    override fun deserialize(bytes: ByteArray?): T? {
        bytes ?: return null
        return protoBuf.decodeFromByteArray(serializer, bytes)
    }
}