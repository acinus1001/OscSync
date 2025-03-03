@file:OptIn(ExperimentalSerializationApi::class)

package dev.kuro9.internal.smartapp.webhook.model

import dev.kuro9.internal.smartapp.webhook.serializer.SmartAppWebhookBodySerializer.ConfirmationDataSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("lifecycle")
sealed interface SmartAppWebhookBody {
    val lifecycle: LifeCycle
    val executionId: String
    val locale: String
    val version: String

    @[SerialName("CONFIRMATION") Serializable]
    data class ConfirmationCycle(
        override val executionId: String,
        override val locale: String,
        override val version: String,
        val appId: String,
        val confirmationData: ConfirmationData,
    ) : SmartAppWebhookBody {
        override val lifecycle = LifeCycle.CONFIRMATION

        @Serializable(with = ConfirmationDataSerializer::class)
        @JsonClassDiscriminator("phase")
        sealed interface ConfirmationData {
            val phase: Phase

            @Serializable
            data class TestPhase(
                val appId: String,
                val confirmationUrl: String,
            ) : ConfirmationData {
                override val phase = Phase.TEST
            }

            @[SerialName("INITIALIZE") Serializable]
            data class InitPhase(
                val installedAppId: String,
                val pageId: String,
                val previousPageId: String,
            ) : ConfirmationData {
                override val phase: Phase = Phase.INITIALIZE
            }

            @[SerialName("PAGE") Serializable]
            data class PagePhase(
                val installedAppId: String,
                val pageId: String,
                val previousPageId: String,
                val config: Map<String, ConfigData>
            ) : ConfirmationData {
                override val phase: Phase = Phase.PAGE

                @Serializable
                @JsonClassDiscriminator("valueType")
                sealed interface ConfigData {

                    @[SerialName("STRING") Serializable]
                    data class StringConfig(
                        val value: String,
                    ) : ConfigData

                    @[SerialName("DEVICE") Serializable]
                    data class DeviceConfig(
                        val deviceId: String,
                        val componentId: String,
                    ) : ConfigData
                }
            }


            enum class Phase {
                TEST,
                INITIALIZE,
                PAGE;
            }
        }
    }

    enum class LifeCycle {
        CONFIRMATION,
    }
}