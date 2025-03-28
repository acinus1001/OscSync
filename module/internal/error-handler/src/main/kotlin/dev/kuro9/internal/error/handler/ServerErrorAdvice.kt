package dev.kuro9.internal.error.handler

import dev.kuro9.common.logger.errorLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnBean(ServerErrorHandler::class)
class ServerErrorAdvice(
    private val handler: List<ServerErrorHandler>,
) {
    private val coroutineErrorHandler = CoroutineExceptionHandler { _, throwable ->
        this.errorLog("exception in coroutine", throwable)
    }

    @EventListener
    fun handle(e: ServerErrorEvent) {
        CoroutineScope(Dispatchers.IO).launch(coroutineErrorHandler) {
            handler.forEach { handler ->
                launch {
                    handler.doHandle(e)
                }
            }
        }
    }
}