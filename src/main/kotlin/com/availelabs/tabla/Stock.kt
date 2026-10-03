package com.availelabs.tabla

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

enum class InstrumentType { COMMON_STOCK, ADR, ETF }

data class StockId(val ticker: String, val listingExchange: String)

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

data class Stock(
    val ticker: String,
    val name: String,
    val listingExchange: String,
    val instrumentType: InstrumentType,
    val sector: String?,
    val summary: String?,
    val dividendHistory: DividendHistory
) {
    val id: StockId get() = StockId(ticker, listingExchange)

    fun normalize(): Stock {
        return copy(
            ticker = ticker.trim().uppercase(),
            name = name.trim(),
            listingExchange = listingExchange.trim(),
            sector = sector?.trim(),
            summary = summary?.trim()
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
