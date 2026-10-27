package com.availelabs.tabla.market

import com.availelabs.tabla.provenance.HistoricalData
import com.availelabs.tabla.provenance.Sourced
import kotlin.uuid.Uuid

sealed interface MarketInstrument {
    val instrumentId: InstrumentId
    val securityListing: Sourced<SecurityListing>
    val incomeHistory: HistoricalData<IncomePayment>
}

@JvmInline
value class InstrumentId(val value: Uuid) {
    companion object {
        fun generate(): InstrumentId =
            InstrumentId(Uuid.generateV7())
    }
}

data class SecuritySymbols(
    val symbol: String,
    val nasdaqSymbol: String,
    val cqsSymbol: String?,
    val edgarSymbol: String
)

data class SecurityListing private constructor(
    val cik: Int,
    val symbols: SecuritySymbols,
    val name: String,
    val exchange: Exchange
) {
    companion object {
        fun of(
            cik: Int,
            symbols: SecuritySymbols,
            name: String,
            exchange: Exchange
        ): SecurityListing {
            require(cik > 0) { "CIK must be a positive integer" }

            val normalizedSymbols = symbols.copy(
                symbol = symbols.symbol.trim().uppercase(),
                nasdaqSymbol = symbols.nasdaqSymbol.trim().uppercase(),
                cqsSymbol = symbols.cqsSymbol?.trim()?.takeIf { it.isNotEmpty() }
            )
            require(normalizedSymbols.symbol.isNotEmpty()) { "Symbol cannot be empty" }
            require(normalizedSymbols.nasdaqSymbol.isNotEmpty()) { "NASDAQ symbol cannot be empty" }

            val normalizedName = name.trim()
            require(normalizedName.isNotEmpty()) { "Name cannot be empty" }

            return SecurityListing(
                cik = cik,
                symbols = normalizedSymbols,
                name = normalizedName,
                exchange = exchange
            )
        }
    }
}

data class Stock(
    override val instrumentId: InstrumentId = InstrumentId.generate(),
    override val securityListing: Sourced<SecurityListing>,
    override val incomeHistory: HistoricalData<IncomePayment>,
) : MarketInstrument

data class Etf(
    override val instrumentId: InstrumentId = InstrumentId.generate(),
    override val securityListing: Sourced<SecurityListing>,
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
