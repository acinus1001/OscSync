package dev.kuro9.internal.error.handler

import io.github.harryjhin.slf4j.extension.error
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
        error(throwable) { "exception in coroutine" }
    }

    @EventListener
    fun handle(e: ServerErrorEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            handler.forEach { handler ->
                launch(coroutineErrorHandler) { handler.doHandle(e) }
            }
        }
    }
}