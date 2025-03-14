package dev.kuro9.internal.smartapp.api.response

import dev.kuro9.internal.smartapp.api.dto.response.SmartAppResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

object SmartAppPagedLinkInfoSerializer : KSerializer<SmartAppResponse.Paged.LinkInfo> {
    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor(
        SmartAppResponse.Paged.LinkInfo::class.qualifiedName!!,
        StructureKind.OBJECT
    ) {
        element("next", JsonObject.serializer().descriptor, isOptional = true)
        element("previous", JsonObject.serializer().descriptor, isOptional = true)
    }

    override fun serialize(
        encoder: Encoder,
        value: SmartAppResponse.Paged.LinkInfo
    ) {

    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): SmartAppResponse.Paged.LinkInfo =
        decoder.decodeStructure(descriptor) {
            var nextObj: JsonObject? = null
            var previousObj: JsonObject? = null

            if (decodeSequentially()) {
                nextObj = decodeSerializableElement(descriptor, 0, JsonObject.serializer())
                previousObj = decodeSerializableElement(descriptor, 1, JsonObject.serializer())
            } else while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> nextObj = decodeSerializableElement(descriptor, 0, JsonObject.serializer())
                    1 -> previousObj = decodeSerializableElement(descriptor, 1, JsonObject.serializer())
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            SmartAppResponse.Paged.LinkInfo(
                next = nextObj?.get("href")?.jsonPrimitive?.content,
                previous = previousObj?.get("href")?.jsonPrimitive?.content
            )
        }
}