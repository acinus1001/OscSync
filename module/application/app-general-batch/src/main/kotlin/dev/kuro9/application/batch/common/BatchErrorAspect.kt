package dev.kuro9.application.batch.common

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@[Aspect Component]
class BatchErrorAspect(private val context: BatchErrorContext) {

    @Around("execution(* dev.kuro9.application.batch..*(..)) && within(@org.springframework.batch.core.configuration.annotation.StepScope *)")
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint): Any? {
        try {
            return joinPoint.proceed()
        } catch (e: Throwable) {
            context.exception = e
            throw e
        }
    }
}