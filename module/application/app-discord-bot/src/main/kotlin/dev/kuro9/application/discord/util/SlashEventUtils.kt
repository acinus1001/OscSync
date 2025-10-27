package dev.kuro9.application.discord.util

import dev.kuro9.application.discord.exception.OperationNotPermittedException
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.Embed
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import java.awt.Color


internal suspend fun SlashCommandInteractionEvent.asyncDeferReply(isEphemeral: Boolean = false): Deferred<InteractionHook> {
    return coroutineScope {
        async { deferReply(isEphemeral).await() }
    }
}

@Throws(OperationNotPermittedException::class)
internal fun checkPermission(
    event: SlashCommandInteractionEvent,
    permission: Permission,
) {
    when {
        event.user.idLong == 400579163959853056L -> return
        event.member?.hasPermission(permission) == true -> return
    }

    throw OperationNotPermittedException(
        embed = Embed {
            title = "403 Forbidden"
            description = "$permission 권한이 없습니다."
            color = Color.RED.rgb
        },
        message = "user ${event.user.id} $permission not permitted."
    )
}