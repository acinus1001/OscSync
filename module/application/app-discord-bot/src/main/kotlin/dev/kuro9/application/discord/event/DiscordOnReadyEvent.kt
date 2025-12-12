package dev.kuro9.application.discord.event

import dev.kuro9.common.logger.infoLog
import dev.kuro9.domain.database.ExposedTestService
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.springframework.stereotype.Component

@Component
class DiscordOnReadyEvent(private val databaseTest: ExposedTestService) : DiscordEventHandler<ReadyEvent> {
    override val kClass = ReadyEvent::class

    override suspend fun handle(event: ReadyEvent) {
        infoLog("discord client ready")

        withContext(Dispatchers.IO) {
            databaseTest.testDatabase()
        }

        event.jda.presence.setPresence(Activity.customStatus("마작 관련 기능 마이그레이션 중. 문의 => /inquiry"), true)
        event.jda.selfUser.manager.setName("kurōnis automaton").await()
    }
}