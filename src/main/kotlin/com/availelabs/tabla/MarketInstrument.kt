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
    val exchange: String
) {
    companion object {
        fun of(ticker: String, exchange: String): Listing {
            val normalizedTicker = ticker.trim().uppercase()
            val normalizedExchange = exchange.trim().uppercase()

            require(normalizedTicker.isNotEmpty())
            require(normalizedExchange.isNotEmpty())

            return Listing(
                ticker = normalizedTicker,
                exchange = normalizedExchange
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
