package dev.kuro9.multiplatform.common.date.util

import kotlinx.datetime.*

fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.of("Asia/Seoul")): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)

fun LocalDate.Companion.now(timeZone: TimeZone = TimeZone.of("Asia/Seoul")): LocalDate =
    LocalDateTime.now(timeZone).date