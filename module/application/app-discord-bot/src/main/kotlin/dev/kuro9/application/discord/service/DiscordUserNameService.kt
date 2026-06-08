package dev.kuro9.application.discord.service

import dev.kuro9.domain.discord.name.service.DiscordSearchService
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import org.springframework.beans.factory.ObjectProvider
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class DiscordUserNameService(
    private val jda: ObjectProvider<JDA>,
    private val discordSearchService: DiscordSearchService,
) {

    @Cacheable(value = ["cache-discord-name"], key = "#userId")
    suspend fun getUserName(userId: Long): String {
        val userName = jda.`object`.retrieveUserById(userId).await().effectiveName
        discordSearchService.updateDiscordName(userId, userName)
        return userName
    }

    @CachePut(value = ["cache-discord-name"], key = "#userId")
    suspend fun putUserNameCache(userId: Long): String {
        val userName = jda.`object`.retrieveUserById(userId).await().effectiveName
        discordSearchService.updateDiscordName(userId, userName)
        return userName
    }
}