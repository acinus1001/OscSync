package dev.kuro9.application.discord.util

import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook


internal suspend fun SlashCommandInteractionEvent.asyncDeferReply(isEphemeral: Boolean = false): Deferred<InteractionHook> {
    return coroutineScope {
        async { deferReply(isEphemeral).await() }
    }
}