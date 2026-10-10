package com.availelabs.tabla

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed interface MarketInstrument {
    val instrumentId: InstrumentId
    val primaryListing: Sourced<Listing>
    val incomeHistory: HistoricalData<IncomePayment>
}

@JvmInline
value class InstrumentId(val value: Uuid) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun generate(): InstrumentId =
            InstrumentId(Uuid.generateV7())
    }
}

data class SecuritySymbols(
    val symbol: String,
    val nasdaqSymbol: String,
    val cqsSymbol: String?
)

@ConsistentCopyVisibility
data class Listing private constructor(
    val symbols: SecuritySymbols,
    val name: String,
    val exchange: Exchange
) {
    companion object {
        fun of(symbols: SecuritySymbols, name: String, exchange: Exchange): Listing {
            val normalizedSymbols = symbols.copy(
                symbol = symbols.symbol.trim().uppercase(),
                nasdaqSymbol = symbols.nasdaqSymbol.trim().uppercase(),
                cqsSymbol = symbols.cqsSymbol?.trim()?.takeIf { it.isNotEmpty() }
            )
            require(normalizedSymbols.symbol.isNotEmpty()) { "Symbol cannot be empty" }
            require(normalizedSymbols.nasdaqSymbol.isNotEmpty()) { "NASDAQ symbol cannot be empty" }

            val normalizedName = name.trim()
            require(normalizedName.isNotEmpty()) { "Name cannot be empty" }

            return Listing(
                symbols = normalizedSymbols,
                name = normalizedName,
                exchange = exchange
            )
        }
    }
}

data class Stock(
    override val instrumentId: InstrumentId = InstrumentId.generate(),
    override val primaryListing: Sourced<Listing>,
    override val incomeHistory: HistoricalData<IncomePayment>,
) : MarketInstrument

data class Etf(
    override val instrumentId: InstrumentId = InstrumentId.generate(),
    override val primaryListing: Sourced<Listing>,
    override val incomeHistory: HistoricalData<IncomePayment>,
) : MarketInstrument

enum class Exchange {
    NASDAQ,
    NYSE_AMERICAN,
    NYSE,
    NYSE_ARCA,
    CBOE_BZX,
    IEX,
    TXSE,
    NYSE_TEXAS,
}
