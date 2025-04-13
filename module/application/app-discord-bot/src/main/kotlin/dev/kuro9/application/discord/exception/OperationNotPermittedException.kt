package dev.kuro9.application.discord.exception

import dev.kuro9.domain.error.handler.discord.exception.DiscordEmbedException
import net.dv8tion.jda.api.entities.MessageEmbed

class OperationNotPermittedException(
    override val embed: MessageEmbed,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : DiscordEmbedException(embed, message, cause)