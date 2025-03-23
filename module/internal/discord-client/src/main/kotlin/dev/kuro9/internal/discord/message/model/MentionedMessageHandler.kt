package dev.kuro9.internal.discord.message.model

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface MentionedMessageHandler {
    suspend fun handleMention(message: String, event: MessageReceivedEvent)
}