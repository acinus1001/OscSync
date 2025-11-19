package dev.kuro9.multiplatform.common.date.util

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.of("Asia/Seoul")): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)

fun LocalDate.Companion.now(timeZone: TimeZone = TimeZone.of("Asia/Seoul")): LocalDate =
    LocalDateTime.now(timeZone).date

fun LocalDate.toRangeOfDay(): ClosedRange<LocalDateTime> = atTime(0, 0)..atTime(23, 59, 59, 59)

fun ClosedRange<LocalDate>.toDateTimeRange(): ClosedRange<LocalDateTime> =
    start.atTime(0, 0)..endInclusive.atTime(23, 59, 59, 59)

fun ClosedRange<LocalDate>.toList(): List<LocalDate> {
    val result = mutableListOf<LocalDate>()
    var current = start
    while (current <= endInclusive) {
        result.add(current)
        current = current.plus(1, DateTimeUnit.DAY)
    }
    return result
}