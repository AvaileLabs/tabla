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

@ConsistentCopyVisibility
data class Listing private constructor(
    val ticker: String,
    val name: String,
    val exchange: Exchange
) {
    companion object {
        fun of(ticker: String, name: String, exchange: Exchange): Listing {
            val normalizedTicker = ticker.trim().uppercase()
            val normalizedName = name.trim()

            require(normalizedTicker.isNotEmpty()) { "Ticker cannot be empty" }
            require(normalizedName.isNotEmpty()) { "Name cannot be empty" }

            return Listing(
                ticker = normalizedTicker,
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
