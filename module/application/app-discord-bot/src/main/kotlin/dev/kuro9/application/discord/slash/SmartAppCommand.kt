package dev.kuro9.application.discord.slash

import dev.kuro9.application.discord.util.asyncDeferReply
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException.DuplicatedRegisterException
import dev.kuro9.domain.smartapp.user.exception.SmartAppDeviceException.NotSupportException
import dev.kuro9.domain.smartapp.user.exception.SmartAppUserException.CredentialNotFoundException
import dev.kuro9.domain.smartapp.user.service.SmartAppUserService
import dev.kuro9.internal.discord.slash.model.SlashCommandComponent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.group
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        subcommand("devices", "list my devices on smartthings server")
        subcommand("register", "register my device") {
            option<String>("device-id", "Paste your device ID to register.", required = true)
            option<String>(
                "device-name",
                "Write name of your device to use.",
                required = true
            )
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

        group("token", "Manage your SmartApp Token") {
            subcommand("register", "Register your SmartApp Token to access your devices") {
                option<String>("token", "https://account.smartthings.com/tokens", required = true)
            }
            subcommand("delete", "Delete your exist SmartApp Token")
        }
    }

    override suspend fun handleEvent(event: SlashCommandInteractionEvent) {
        val isEphemeral = when (event.subcommandGroup) {
            "token" -> when (event.subcommandName) {
                "register" -> true
                else -> false
            }

            else -> false
        }
        val deferReply: Deferred<InteractionHook> = event.asyncDeferReply(isEphemeral)

        runCatching {
            when (event.subcommandGroup) {
                "token" -> when (event.subcommandName) {
                    "register" -> registerToken(event, deferReply)
                    "delete" -> deleteToken(event, deferReply)
                    else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
                }

                else -> when (event.subcommandName) {
                    "devices" -> listDevices(event, deferReply)
                    "register" -> registerDevice(event, deferReply)
                    "registered" -> listRegisteredDevices(event, deferReply)

                    "execute" -> executeDevice(event, deferReply)
                    "delete" -> deleteDevice(event, deferReply)
                    else -> throw NotImplementedError("Unknown command=${event.fullCommandName}")
                }
            }

        }.onFailure { t ->
            deferReply
                .await()
                .editOriginalEmbeds(getDefaultExceptionEmbed(t))
                .await()
            return
        }
    }

    override suspend fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        when (event.subcommandName) {
            "execute", "delete" -> handleDeviceListAutoComplete(event)
            else -> return
        }
    }

    private suspend fun listDevices(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
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
        deferReply.await().editOriginalEmbeds(embed).await()
    }

    private suspend fun listRegisteredDevices(
        event: SlashCommandInteractionEvent,
        deferReply: Deferred<InteractionHook>
    ) {
        val devices = withContext(Dispatchers.IO) {
            smartAppUserService.getUserRegisteredDevices(event.user.idLong)
        }

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
        }.let { deferReply.await().editOriginalEmbeds(it).await(); return }
    }

    private suspend fun registerDevice(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        val deviceName = smartAppUserService.registerDeviceWithId(
            userId = event.user.idLong,
            deviceId = event.getOption("device-id")!!.asString,
            event.getOption("device-name")!!.asString
        )

        Embed {
            title = "Device Registered"
            description = "DEVICE=`$deviceName`"
            color = Color.GREEN.rgb
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun executeDevice(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
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
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun deleteDevice(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
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
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun registerToken(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        withContext(Dispatchers.IO) {
            smartAppUserService.saveUserCredential(
                userId = event.user.idLong,
                smartAppToken = event.getOption("token")!!.asString
            )
        }

        Embed {
            title = "Token Registered"
            color = Color.GREEN.rgb
        }.let { deferReply.await().editOriginalEmbeds(it).await(); return }
    }

    private suspend fun deleteToken(event: SlashCommandInteractionEvent, deferReply: Deferred<InteractionHook>) {
        withContext(Dispatchers.IO) {
            smartAppUserService.deleteUserCredential(event.user.idLong)
        }

        Embed {
            title = "Token Deleted"
            color = Color.GREEN.rgb
        }.let { deferReply.await().editOriginalEmbeds(it).await() }
    }

    private suspend fun handleDeviceListAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name != "device-name") return

        withContext(Dispatchers.IO) {
            smartAppUserService.getRegisteredDevices(event.user.idLong)
        }
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

            is NotImplementedError -> Embed {
                title = "Not Implemented"
                description = "This command is not implemented. Contact <@400579163959853056> to report."
                color = Color.RED.rgb
            }

            else -> throw t
        }

}