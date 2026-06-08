package dev.kuro9.application.homepage.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.AbstractKotlinSerializationHttpMessageConverter
import org.springframework.stereotype.Component

@OptIn(ExperimentalSerializationApi::class)
@Component
class KotlinSerializationProtobufHttpMessageConverter(
    protoBuf: ProtoBuf = ProtoBuf {
        this.encodeDefaults = false
    }
) : AbstractKotlinSerializationHttpMessageConverter<ProtoBuf>(
    protoBuf,
    MediaType.APPLICATION_PROTOBUF
) {
    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return canRead(mediaType)
    }

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return canWrite(mediaType)
    }

    override fun readInternal(
        serializer: KSerializer<in Any>,
        format: ProtoBuf,
        inputMessage: HttpInputMessage
    ): Any {
        return inputMessage.body.use { inputStream ->
            inputStream.readAllBytes().let {
                format.decodeFromByteArray(serializer, it)
                    ?: throw IllegalStateException("Failed to deserialize protobuf body")
            }
        }
    }

    override fun writeInternal(
        `object`: Any,
        serializer: KSerializer<in Any>,
        format: ProtoBuf,
        outputMessage: HttpOutputMessage
    ) {
        outputMessage.body.use { outputStream ->
            val bytes = format.encodeToByteArray(serializer, `object`)
            outputStream.write(bytes)
            outputStream.flush() // 버퍼에 남은 잔여 데이터 비우기
        }
    }

}