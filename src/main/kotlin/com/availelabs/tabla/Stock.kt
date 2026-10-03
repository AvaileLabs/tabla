package com.availelabs.tabla

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.uuid.Uuid

enum class InstrumentType { COMMON_STOCK, ADR, ETF }

data class DividendHistory(
    val status: Status = Status.Unknown,
    val lastChecked: LocalDate?
) {
    sealed interface Status {
        data object Unknown : Status
        data object NeverPaid : Status
        data class Paid(val history: List<Dividend>) : Status
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
    val id: StockId,
    val profile: StockProfile,
    val dividendHistory: DividendHistory
) {
    fun normalize(): Stock {
        return copy(
            profile = profile.normalize()
        )
    }
}

data class Dividend(
    val declarationDate: LocalDate?,
    val exDate: LocalDate,
    val recordDate: LocalDate?,
    val paymentDate: LocalDate?,
    val amountPerShare: BigDecimal,
    val currency: Currency
)

@JvmInline
value class StockId(val value: Uuid)