package com.availelabs.tabla

import java.net.URI
import java.time.LocalDate
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

data class SourceInfo(
    val source: DataSource,
    val uri: URI,
    val retrievedAt: Instant
)

enum class DataSource {
    NASDAQ_TRADER
}

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