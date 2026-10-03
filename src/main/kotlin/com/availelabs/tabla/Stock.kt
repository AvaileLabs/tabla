package com.availelabs.tabla

import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency


enum class InstrumentType { COMMON_STOCK, ADR, ETF }

data class StockId(val ticker: String, val listingExchange: String)

data class Stock(
    val ticker: String,
    val name: String,
    val listingExchange: String,
    val instrumentType: InstrumentType,
    val sector: String?,
    val summary: String?,
    val dividendHistory: List<Dividend>
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
