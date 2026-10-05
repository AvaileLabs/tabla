package com.availelabs.tabla

import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.time.Instant

data class DateCoverage(
    val from: LocalDate,
    val to: LocalDate,
) {
    init {
        require(!to.isBefore(from)) {
            "Coverage end date must not precede start date"
        }
    }
}

/**
 * Preserves temporal information as provided by the source without
 * assuming additional precision or time-zone information.
 */
sealed interface SourceTimestamp {
    data class Date(val value: LocalDate) : SourceTimestamp

    data class DateTime(val value: LocalDateTime) : SourceTimestamp

    data class Instant(val value: kotlin.time.Instant) : SourceTimestamp
}

data class SourceInfo(
    val uri: URI,
    val retrievedAt: Instant,
    val createdAt: SourceTimestamp?
)

data class Sourced<T>(
    val value: T,
    val sourceInfo: SourceInfo
)

fun <T> T.sourcedBy(sourceInfo: SourceInfo): Sourced<T> =
    Sourced(
        value = this,
        sourceInfo = sourceInfo
    )

data class HistoricalData<T>(
    val segments: List<HistoricalDataSegment<T>>
)

data class HistoricalDataSegment<T>(
    val observations: List<T>,
    val coverage: DateCoverage,
    val sourceInfo: SourceInfo
)
