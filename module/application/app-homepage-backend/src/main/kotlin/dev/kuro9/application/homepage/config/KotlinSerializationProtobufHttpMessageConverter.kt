package dev.kuro9.application.homepage.config

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
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
        return isProtobufMediaType(mediaType) && super.canRead(clazz, mediaType)
    }

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return isProtobufMediaType(mediaType) && super.canWrite(clazz, mediaType)
    }

    override fun readInternal(
        serializer: KSerializer<in Any>,
        format: ProtoBuf,
        inputMessage: HttpInputMessage
    ): Any {
        val bytes = inputMessage.body.readAllBytes()
        return format.decodeFromByteArray(serializer, bytes) ?: throw SerializationException("null")
    }

    override fun writeInternal(
        `object`: Any,
        serializer: KSerializer<in Any>,
        format: ProtoBuf,
        outputMessage: HttpOutputMessage
    ) {
        val bytes = format.encodeToByteArray(serializer, `object`)
        outputMessage.body.write(bytes)
        outputMessage.body.flush()
    }

    private fun isProtobufMediaType(mediaType: MediaType?): Boolean {
        return mediaType != null && MediaType.APPLICATION_PROTOBUF.includes(mediaType)
    }

}