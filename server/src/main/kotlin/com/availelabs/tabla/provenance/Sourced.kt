package com.availelabs.tabla.provenance

import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.net.URI
import kotlin.time.Instant

value class DateCoverage(
    val from: LocalDate,
    val to: LocalDate,
) {
    init {
        require(to >= from) {
            "Coverage end date must not precede start date"
        }
    }
}

/**
 * Preserves temporal information as provided by the source without
 * assuming additional precision or time-zone information.
 */
sealed interface SourceTimestamp {
    value class Date(val value: LocalDate) : SourceTimestamp

    value class DateTime(val value: LocalDateTime) : SourceTimestamp

    value class Instant(val value: kotlin.time.Instant) : SourceTimestamp
}

value class SourceInfo(
    val uri: URI,
    val retrievedAt: Instant,
    val createdAt: SourceTimestamp?
)

value class Sourced<T>(
    val value: T,
    val sourceInfo: SourceInfo
)

/** Attaches source metadata to any value. */
fun <T> T.sourcedBy(
    uri: URI,
    retrievedAt: Instant,
    createdAt: SourceTimestamp? = null
): Sourced<T> =
    Sourced(
        value = this,
        sourceInfo = SourceInfo(uri, retrievedAt, createdAt)
    )

value class HistoricalData<T>(
    val segments: ImmutableList<HistoricalDataSegment<T>>
)

value class HistoricalDataSegment<T>(
    val observations: ImmutableList<T>,
    val coverage: DateCoverage,
    val sourceInfo: SourceInfo
)
