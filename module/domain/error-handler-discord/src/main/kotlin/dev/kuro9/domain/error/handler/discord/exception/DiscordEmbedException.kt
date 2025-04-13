package dev.kuro9.domain.error.handler.discord.exception

import net.dv8tion.jda.api.entities.MessageEmbed

abstract class DiscordEmbedException(
    open val embed: MessageEmbed,
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException()
