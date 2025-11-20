package dev.kuro9.application.batch.common

import com.navercorp.spring.batch.plus.kotlin.configuration.StepTransitionBuilderDsl
import com.navercorp.spring.batch.plus.kotlin.configuration.TransitionBuilderDsl
import org.springframework.batch.core.ExitStatus

fun <T : Any> StepTransitionBuilderDsl<T>.handleFlow(onComplete: TransitionBuilderDsl<T>.() -> Unit) {
    on(ExitStatus.COMPLETED.exitCode) {
        onComplete()
    }

    on(ExitStatus.FAILED.exitCode) {
        stepBean("batchFailureNotifyStep")
        fail()
    }
    on(ExitStatus.UNKNOWN.exitCode) {
        stepBean("batchFailureNotifyStep")
        fail()
    }
}