package dev.kuro9.internal.smartapp.webhook.serializer

import dev.kuro9.internal.smartapp.webhook.model.SmartAppWebhookBody.ConfirmationCycle.ConfirmationData
import dev.kuro9.internal.smartapp.webhook.model.SmartAppWebhookBody.ConfirmationCycle.ConfirmationData.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface SmartAppWebhookBodySerializer {

    object ConfirmationDataSerializer : JsonContentPolymorphicSerializer<ConfirmationData>(ConfirmationData::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ConfirmationData> {
            return when (val phase = element.jsonObject["phase"]?.jsonPrimitive?.content) {
                null -> TestPhase.serializer()
                "INITIALIZE" -> InitPhase.serializer()
                "PAGE" -> PagePhase.serializer()
                else -> throw IllegalArgumentException("Unknown phase type : $phase")
            }
        }

    }
}