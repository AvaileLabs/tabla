package com.availelabs.tabla

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class InstrumentType { COMMON_STOCK, ADR, ETF }

data class CashDividend(
    val declarationDate: LocalDate?,
    val exDate: LocalDate?,
    val recordDate: LocalDate?,
    val paymentDate: LocalDate?,
    val amountPerShare: BigDecimal,
    val currency: Currency
)

data class StockSplit(
    val announcementDate: LocalDate?,
    val exDate: LocalDate?,
    val recordDate: LocalDate?,
    val effectiveDate: LocalDate,
    val numerator: Int,
    val denominator: Int
)

data class ShareCountSnapshot(
    val date: LocalDate,
    val sharesOutstanding: BigDecimal
)

data class DividendHistory(
    val status: Status = Status.Unknown,
    val lastChecked: LocalDate?
) {
    sealed interface Status {
        data object Unknown : Status
        data object NeverPaid : Status
        data class Paid(val history: List<CashDividend>) : Status
    }
}

data class StockSplitHistory(
    val status: Status = Status.Unknown,
    val lastChecked: LocalDate?
) {
    sealed interface Status {
        data object Unknown : Status
        data object NeverSplit : Status
        data class Split(val history: List<StockSplit>) : Status
    }
}

data class ShareCountHistory(
    val status: Status = Status.Unknown,
    val lastChecked: LocalDate?
) {
    sealed interface Status {
        data object Unknown : Status
        data class Known(val history: List<ShareCountSnapshot>) : Status
    }
}

data class StockProfile(
    val ticker: String,
    val name: String,
    val listingExchange: String,
    val instrumentType: InstrumentType,
    val sector: String?,
    val summary: String?,
    val lastChecked: LocalDate?
) {
    fun normalize(): StockProfile {
        return copy(
            ticker = ticker.trim().uppercase(),
            name = name.trim(),
            listingExchange = listingExchange.trim(),
            sector = sector?.trim(),
            summary = summary?.trim()
        )
    }
}

data class Stock(
    val id: StockId = StockId.generate(),
    val profile: StockProfile,
    val dividendHistory: DividendHistory,
    val stockSplitHistory: StockSplitHistory,
    val shareCountHistory: ShareCountHistory
) {
    fun normalize(): Stock {
        return copy(
            profile = profile.normalize()
        )
    }
}

@JvmInline
value class StockId(val value: Uuid) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun generate(): StockId =
            StockId(Uuid.generateV7())
    }
}
