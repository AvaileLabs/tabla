package com.availelabs.tabla

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import java.net.URI
import kotlin.time.Instant

data class DateCoverage(
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

/**
 * Attaches source metadata to raw bytes, wrapping them in a source-specific
 * type (e.g., EdgarCompanyTickersRawFile instead of ByteArray) so function
 * parameters and return types can distinguish one source's ByteArray
 * from another's.
 */
fun <T : SourceFile> ByteArray.sourcedBy(
    source: SnapshotSource<T>,
    retrievedAt: Instant,
    createdAt: SourceTimestamp? = null
): Sourced<T> =
    source.wrap(this).sourcedBy(
        uri = source.uri,
        retrievedAt = retrievedAt,
        createdAt = createdAt
    )

data class HistoricalData<T>(
    val segments: List<HistoricalDataSegment<T>>
)

data class HistoricalDataSegment<T>(
    val observations: List<T>,
    val coverage: DateCoverage,
    val sourceInfo: SourceInfo
)
