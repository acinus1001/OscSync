package dev.kuro9.application.discord.slash

import dev.kuro9.common.logger.errorLog
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException.DuplicatedRegisterException
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException.NotSupportException
import dev.kuro9.domain.smartapp.user.exception.SmartAppUserException.CredentialNotFoundException
import dev.kuro9.domain.smartapp.user.service.SmartAppUserService
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component
import java.awt.Color

@Component
class SmartAppCommand(
    private val smartAppUserService: SmartAppUserService,
) : SlashCommandComponent {
    override val commandData: SlashCommandData = Command("iot", "control iot devices") {
        subcommand("devices", "list my devices")
        subcommand("register", "register my device") {
            option<String>("device-id", "Paste your device ID to register.", required = true)
            option<String>("device-name", "Write name of your device to use.", required = false)
        }
        subcommand("execute", "execute my device") {
            option<String>(
                "device-name",
                "Enter your aliased device name to execute",
                required = true,
                autocomplete = true
            )
            option<Boolean>("desire-state", "Enter your desire state", required = true)
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val deferReply = event.deferReply().await()
        runCatching {
            when (event.subcommandName) {
                "devices" -> listDevices(event, deferReply)
                "register" -> registerDevice(event, deferReply)
                "execute" -> executeDevice(event, deferReply)
                else -> throw IllegalArgumentException("Unknown command=${event.fullCommandName}")
            }
        }.onFailure {
            getDefaultExceptionEmbed(it).let { deferReply.editOriginalEmbeds(it).await(); return }
        }
    }

    override suspend fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        when (event.subcommandName) {
            "execute" -> handleDeviceListAutoComplete(event)
            else -> return
        }
    }

    private suspend fun listDevices(event: SlashCommandInteractionEvent, deferReply: InteractionHook) {

        val devices = smartAppUserService.getUserDevices(event.user.idLong)

        val embed = Embed {
            title = "Device List of `${event.user.name}`"
            description = "use `/iot register` to handle device"
            devices.items.forEach { device ->
                field {
                    name = device.name ?: device.deviceId
                    value = "ID=`${device.deviceId}`"
                    inline = false
                }
            }
        }
        deferReply.editOriginalEmbeds(embed).await()
    }

    private suspend fun registerDevice(event: SlashCommandInteractionEvent, deferReply: InteractionHook) {

        val deviceName = smartAppUserService.registerDeviceWithId(
            userId = event.user.idLong,
            deviceId = event.getOption("device-id")!!.asString,
            event.getOption("device-name")!!.asString
        )

        Embed {
            title = "Device Registered"
            color = Color.GREEN.rgb
            description = "DEVICE=$deviceName"
        }.let { deferReply.editOriginalEmbeds(it).await() }
    }

    private suspend fun executeDevice(event: SlashCommandInteractionEvent, deferReply: InteractionHook) {
        val deviceName = event.getOption("device-name")!!.asString
        val desireState = event.getOption("desire-state")!!.asBoolean

        smartAppUserService.executeDeviceByName(
            userId = event.user.idLong,
            deviceName = deviceName,
            desireState = desireState,
        )

        Embed {
            title = "Device Executed"
            description = "Current State is : ${if (desireState) "ON" else "OFF"}"
            color = Color.GREEN.rgb
        }.let { deferReply.editOriginalEmbeds(it).await() }
    }

    private suspend fun handleDeviceListAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name != "device-name") return

        // 캐시 처ㅣㄹ 해주세요\
        smartAppUserService.getRegisteredDevices(event.user.idLong)
            .filter { it.deviceName.startsWith(event.focusedOption.value) }
            .map { it.deviceName }
            .let(event::replyChoiceStrings)
            .await()
    }

    private fun getDefaultExceptionEmbed(t: Throwable): MessageEmbed =
        when (t) {
            is CredentialNotFoundException -> Embed {
                title = "SmartApp Credential Not Registered"
                description = "Use `//TODO` to register token"
                color = Color.RED.rgb
            }

            is DuplicatedRegisterException -> Embed {
                title = "Already Registered Device"
                color = Color.ORANGE.rgb
            }

            is NotSupportException -> Embed {
                title = "Device Not Support"
                description = "This device is not supported."
                color = Color.RED.rgb
            }

            is SmartAppDeviceException.NotFoundException -> Embed {
                title = "Device Not Found"
                description = "Check your input."
                color = Color.RED.rgb
            }

            else -> {
                errorLog("Unknown error", t)
                Embed {
                    title = "Unknown Error"
                    description = "Contact <@400579163959853056> to report."
                    color = Color.RED.rgb
                }
            }
        }

}