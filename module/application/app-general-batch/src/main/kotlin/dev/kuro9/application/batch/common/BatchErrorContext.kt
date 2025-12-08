package dev.kuro9.application.batch.common

import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.stereotype.Component

@[JobScope Component]
class BatchErrorContext {
    final var exception: Throwable? = null;
}