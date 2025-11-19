package dev.kuro9.application.discord.service

import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.JDA
import org.springframework.beans.factory.ObjectProvider
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class DiscordUserNameService(private val jda: ObjectProvider<JDA>) {

    @Cacheable(value = ["cache-discord-name"], key = "#userId")
    suspend fun getUserName(userId: Long): String {
        return jda.`object`.retrieveUserById(userId).await().effectiveName
    }
}