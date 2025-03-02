package dev.kuro9.application.discord.slash

import dev.kuro9.common.logger.infoLog
import dev.kuro9.domain.smartapp.user.service.SmartAppUserService
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.kuro9.internal.smartapp.client.SmartAppApiClient
import dev.kuro9.internal.smartapp.model.request.SmartAppToken
import dev.kuro9.internal.smartapp.model.response.SmartAppDeviceListResponse
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands.slash
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.jetbrains.exposed.sql.Database
import org.springframework.stereotype.Component

@Component
class SmartAppCommand(
    private val smartAppClient: SmartAppApiClient,
    private val smartAppUserService: SmartAppUserService,
    private val database: Database
) : SlashCommandComponent {
    override val commandData: SlashCommandData = slash("iot", "control iot devices")
        .subcommand("devices", "list devices")

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply = event.deferReply().await()
        when (event.subcommandName) {
            "devices" -> {
                val token = smartAppUserService.getUserCredential(event.user.idLong)

                token ?: deferReply.editOriginal("token not exists").await()
                token ?: return

                infoLog("token: $token")
                val devices = smartAppClient.listDevices(
                    smartAppToken = SmartAppToken.of(token)
                )
                val embed = Embed {
                    title = "device list of user ${event.user.name}"
                    devices.items.forEach { device: SmartAppDeviceListResponse.DeviceInfo ->
                        field {
                            name = device.name ?: device.deviceId
                            value = device.manufacturerName
                        }
                    }
                }
                deferReply.editOriginalEmbeds(embed).await()
            }

            else -> throw IllegalArgumentException("Unknown command=${event.fullCommandName}")
        }
    }

}