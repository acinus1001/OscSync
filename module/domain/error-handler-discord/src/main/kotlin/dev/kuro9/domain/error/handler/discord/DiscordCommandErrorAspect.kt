package dev.kuro9.domain.error.handler.discord

import kotlinx.coroutines.reactor.awaitSingleOrNull
import net.dv8tion.jda.api.events.GenericEvent
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

@[Aspect Component]
class DiscordCommandErrorAspect(
    private val eventPublisher: ApplicationEventPublisher
) {

    @Around(
        """
        (
            @within(DiscordCommandErrorHandle) || 
            @annotation(DiscordCommandErrorHandle) || 
            execution(* dev.kuro9.internal.discord.slash.model.SlashCommandComponent.handleEvent(..)) || 
            execution(* dev.kuro9.internal.discord.handler.model.ButtonInteractionHandler.handleButtonInteraction(..)) || 
            execution(* dev.kuro9.internal.discord.handler.model.ModalInteractionHandler.handleModalInteraction(..)) ||
            execution(* dev.kuro9.internal.discord.handler.model.MentionedMessageHandler.handleMention(..))
        ) && args(event, ..)
        """
    )
    fun onError(joinPoint: ProceedingJoinPoint, event: GenericEvent): Any? {
        return joinPoint.runCoroutine {
            joinPoint.proceedCoroutine().let { rtn ->
                try {
                    return@let when (rtn) {
                        is Mono<*> -> rtn.awaitSingleOrNull()
                        else -> rtn
                    }
                } catch (t: Throwable) {
                    eventPublisher.publishEvent(DiscordErrorEvent(t, event))
                }

            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val ProceedingJoinPoint.coroutineContinuation: Continuation<Any?>
        get() = this.args.last() as Continuation<Any?>

    private val ProceedingJoinPoint.coroutineArgs: Array<Any?>
        get() = this.args.sliceArray(0 until this.args.size - 1)

    private suspend fun ProceedingJoinPoint.proceedCoroutine(
        args: Array<Any?> = this.coroutineArgs
    ): Any? =
        suspendCoroutineUninterceptedOrReturn { continuation ->
            this.proceed(args + continuation)
        }

    private fun ProceedingJoinPoint.runCoroutine(
        block: suspend () -> Any?
    ): Any? =
        block.startCoroutineUninterceptedOrReturn(this.coroutineContinuation)

}