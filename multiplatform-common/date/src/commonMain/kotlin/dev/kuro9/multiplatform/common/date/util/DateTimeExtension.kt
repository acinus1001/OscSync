package dev.kuro9.multiplatform.common.date.util

import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val TIMEZONE = TimeZone.of("Asia/Seoul")

@OptIn(ExperimentalTime::class)
fun LocalDateTime.Companion.now(timeZone: TimeZone = TIMEZONE): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)

fun LocalDate.Companion.now(timeZone: TimeZone = TIMEZONE): LocalDate =
    LocalDateTime.now(timeZone).date

fun LocalTime.Companion.now(timeZone: TimeZone = TIMEZONE): LocalTime =
    LocalDateTime.now(timeZone).time

fun LocalDate.toRangeOfDay(): ClosedRange<LocalDateTime> = atTime(0, 0)..atTime(23, 59, 59, 59)

fun YearMonth.toRangeOfMonth(): ClosedRange<LocalDateTime> {
    return this.firstDay.atTime(0, 0)..this.lastDay.atTime(23, 59, 59, 999999999)
}

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

fun Instant.toLocalDateTime(): LocalDateTime = toLocalDateTime(TIMEZONE)

operator fun LocalDateTime.plus(other: Duration): LocalDateTime {
    return this.toInstant(TIMEZONE)
        .plus(other)
        .toLocalDateTime()
}
