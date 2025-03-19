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
        subcommand("token", "Register your SmartApp Token to access your devices") {
            option<String>("token", "https://account.smartthings.com/tokens", required = true)
        }
        subcommand("devices", "list my devices on smartthings server")
        subcommand("register", "register my device") {
            option<String>("device-id", "Paste your device ID to register.", required = true)
            option<String>("device-name", "Write name of your device to use.", required = false)
        }
        subcommand("registered", "list my registered devices")
        subcommand("execute", "execute my device") {
            option<String>(
                "device-name",
                "Enter your aliased device name to execute",
                required = true,
                autocomplete = true
            )
            option<Boolean>("desire-state", "Enter your desire state", required = true)
        }
        subcommand("delete", "delete my device") {
            option<String>(
                "device-name",
                "Enter your aliased device name to delete",
                required = true,
                autocomplete = true
            )
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        var deferReply: InteractionHook? = null
        runCatching {
            when (event.subcommandName) {
                "token" -> registerToken(event, event.deferReply(true).await().also { deferReply = it })
                "devices" -> listDevices(event, event.deferReply(false).await().also { deferReply = it })
                "register" -> registerDevice(event, event.deferReply(false).await().also { deferReply = it })
                "registered" -> listRegisteredDevices(event, event.deferReply(false).await().also { deferReply = it })
                "execute" -> executeDevice(event, event.deferReply(false).await().also { deferReply = it })
                "delete" -> deleteDevice(event, event.deferReply(false).await().also { deferReply = it })
                else -> throw IllegalArgumentException("Unknown command=${event.fullCommandName}")
            }
        }.onFailure {
            getDefaultExceptionEmbed(it).let {
                when (deferReply) {
                    null -> event.replyEmbeds(it).await()
                    else -> deferReply.editOriginalEmbeds(it).await()
                }
                return
            }
        }
    }

    override suspend fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        when (event.subcommandName) {
            "execute", "delete" -> handleDeviceListAutoComplete(event)
            else -> return
        }
    }

    private suspend fun listDevices(event: SlashCommandInteractionEvent, deferReply: InteractionHook) {

        val devices = smartAppUserService.getUserDevices(event.user.idLong)

        val embed = Embed {
            title = "Device list of `${event.user.name}`"
            description = "use `/iot register` to register device"
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

    private suspend fun listRegisteredDevices(event: SlashCommandInteractionEvent, deferReply: InteractionHook) {
        val devices = smartAppUserService.getUserRegisteredDevices(event.user.idLong)

        Embed {
            title = "Registered device list of `${event.user.name}`"
            description = "use `/iot execute` to handle device"
            devices.forEach { device ->
                field {
                    name = device.deviceName
                    value = "ID=`${device.deviceId}`"
                    inline = false
                }
            }
        }.let { deferReply.editOriginalEmbeds(it).await(); return }
    }

    private suspend fun registerDevice(event: SlashCommandInteractionEvent, deferReply: InteractionHook) {

        val deviceName = smartAppUserService.registerDeviceWithId(
            userId = event.user.idLong,
            deviceId = event.getOption("device-id")!!.asString,
            event.getOption("device-name")!!.asString
        )

        Embed {
            title = "Device Registered"
            description = "DEVICE=`$deviceName`"
            color = Color.GREEN.rgb
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
            description = "Current State is : `${if (desireState) "ON" else "OFF"}`"
            color = Color.GREEN.rgb
        }.let { deferReply.editOriginalEmbeds(it).await() }
    }

    private suspend fun deleteDevice(event: SlashCommandInteractionEvent, deferReply: InteractionHook) {
        val deviceName = event.getOption("device-name")!!.asString

        val hasDeleted = smartAppUserService.deleteDeviceByName(
            userId = event.user.idLong,
            deviceName = deviceName,
        )

        when (hasDeleted) {
            true -> Embed {
                title = "Device Deleted"
                description = "Device deleted: `$deviceName`"
                color = Color.GREEN.rgb
            }

            false -> Embed {
                title = "Device not found"
                description = "Device not found: `$deviceName`"
                color = Color.YELLOW.rgb
            }
        }.let { deferReply.editOriginalEmbeds(it).await() }
    }

    private suspend fun registerToken(event: SlashCommandInteractionEvent, deferReply: InteractionHook) {
        smartAppUserService.saveUserCredential(
            userId = event.user.idLong,
            smartAppToken = event.getOption("token")!!.asString
        )

        Embed {
            title = "Token Registered"
            color = Color.GREEN.rgb
        }.let { deferReply.editOriginalEmbeds(it).await(); return }
    }

    private suspend fun handleDeviceListAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name != "device-name") return

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
                description = "Use `/iot token` to register token"
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