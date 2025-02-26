package dev.kuro9.internal.smartapp.model.request

import kotlinx.serialization.Serializable

@Serializable
data class SmartAppDeviceCommandRequest(
    val commands: List<Command>
) {

    @Serializable
    data class Command(
        val component: String,
        val capability: String,
        val command: String,
        val arguments: List<String>,
    ) {
        companion object {

            fun switch(statusTo: Boolean): Command = Command(
                component = "main",
                capability = "switch",
                command = if (statusTo) "on" else "off",
                arguments = emptyList(),
            )
        }
    }

}