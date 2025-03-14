package dev.kuro9.internal.smartapp.api.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SmartAppDeviceCommandRequest(
    val commands: List<Command>
) {

    constructor(vararg commands: Command) : this(listOf(*commands))


    companion object {

        fun switch(statusTo: Boolean) = SmartAppDeviceCommandRequest(
            Command(
                component = "main",
                capability = "switch",
                command = if (statusTo) "on" else "off",
                arguments = emptyList(),
            )
        )
    }

    @Serializable
    data class Command(
        val component: String,
        val capability: String,
        val command: String,
        val arguments: List<String>,
    )
}