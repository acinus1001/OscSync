package dev.kuro9.application.batch.f1.news.tasklet.dto

import dev.kuro9.domain.f1.dto.F1NewsDto
import dev.kuro9.domain.webhook.repository.table.WebhookSubscribeChannelEntity

data class F1NewsTargetWebhookDto(
    val webhookInfo: WebhookSubscribeChannelEntity,
    val sendNewsInfoList: List<F1NewsDto>,
)