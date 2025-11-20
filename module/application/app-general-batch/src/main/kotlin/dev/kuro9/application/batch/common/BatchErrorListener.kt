package dev.kuro9.application.batch.common

import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.stereotype.Component

@Component
class BatchErrorListener : ChunkListener {

    override fun afterChunkError(context: ChunkContext) {
        context.stepContext.stepExecution.executionContext.put("step", context.stepContext.stepName)

        val exception = context.stepContext.stepExecution.failureExceptions.firstOrNull()
        if (exception != null) {
            context.stepContext.stepExecution.executionContext.put(
                "exception",
                exception
            )
        }
    }
}