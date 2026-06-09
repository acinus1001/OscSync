package dev.kuro9.domain.cache.serializer

import dev.kuro9.multiplatform.common.serialization.minifyJson
import kotlinx.serialization.KSerializer
import org.springframework.data.redis.serializer.RedisSerializer

class KotlinRedisJsonSerializer<T>(private val serializer: KSerializer<T>) : RedisSerializer<T> {

    override fun serialize(value: T?): ByteArray? {
        value ?: return null
        return minifyJson.encodeToString(serializer, value).toByteArray(Charsets.UTF_8)
    }

    override fun deserialize(bytes: ByteArray?): T? {
        bytes ?: return null
        return minifyJson.decodeFromString(serializer, bytes.toString(Charsets.UTF_8))
    }
}