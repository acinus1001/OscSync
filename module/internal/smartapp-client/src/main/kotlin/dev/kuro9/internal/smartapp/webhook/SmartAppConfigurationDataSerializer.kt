package dev.kuro9.internal.smartapp.webhook.serializer

import dev.kuro9.internal.smartapp.webhook.model.SmartAppWebhookResponse.ConfigurationData.InitData
import dev.kuro9.multiplatform.common.serialization.minifyJson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

interface SmartAppConfigurationDataSerializer {

    object InitDataSerializer : KSerializer<InitData> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor(InitData::class.qualifiedName!!) {
                element("page", InitData.serializer().descriptor)
            }

        override fun serialize(encoder: Encoder, value: InitData) {
            val jsonEncoder = encoder as? JsonEncoder
                ?: throw SerializationException("This serializer can only be used with JSON")

            val jsonObject = buildJsonObject {
                put("page", minifyJson.encodeToJsonElement(InitData.serializer(), value))
            }

            jsonEncoder.encodeJsonElement(jsonObject)
        }

        override fun deserialize(decoder: Decoder): InitData {
            val jsonDecoder = decoder as? JsonDecoder
                ?: throw SerializationException("This serializer can only be used with JSON")

            val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
            val pageElement = jsonObject["page"]
                ?: throw SerializationException("Missing 'page' key in JSON")

            return minifyJson.decodeFromJsonElement(InitData.serializer(), pageElement)
        }
    }
}