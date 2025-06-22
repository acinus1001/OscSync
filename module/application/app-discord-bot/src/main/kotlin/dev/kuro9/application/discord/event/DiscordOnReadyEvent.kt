package dev.kuro9.application.discord.event

import dev.kuro9.common.logger.infoLog
import dev.kuro9.internal.discord.model.DiscordEventHandler
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.session.ReadyEvent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component

@Component
class DiscordOnReadyEvent(private val database: Database) : DiscordEventHandler<ReadyEvent> {
    override val kClass = ReadyEvent::class

    override suspend fun handle(event: ReadyEvent) {
        transaction(database) {
            infoLog("discord client ready")
            Table.Dual.select(stringLiteral("hello, world!"))
                .first()
                .get(stringLiteral("hello, world!"))
                .also { infoLog("db : $it") }

        }

        event.jda.presence.setPresence(Activity.customStatus("마작 관련 기능 마이그레이션 중. 문의 => @kurovine9"), true)
        event.jda.selfUser.manager.setName("KGB").await()
    }
}