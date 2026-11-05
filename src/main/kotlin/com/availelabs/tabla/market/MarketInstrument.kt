package com.availelabs.tabla.market

import com.availelabs.tabla.provenance.HistoricalData
import com.availelabs.tabla.provenance.Sourced
import kotlin.uuid.Uuid

sealed interface MarketInstrument {
    val instrumentId: InstrumentId
    val securityListing: Sourced<SecurityListing>
    val incomeHistory: HistoricalData<IncomePayment>
}

value class InstrumentId(val value: Uuid) {
    companion object {
        fun generate(): InstrumentId =
            InstrumentId(Uuid.generateV7())
    }
}

value class Stock(
    override val instrumentId: InstrumentId = InstrumentId.generate(),
    override val securityListing: Sourced<SecurityListing>,
    override val incomeHistory: HistoricalData<IncomePayment>,
) : MarketInstrument

value class Etf(
    override val instrumentId: InstrumentId = InstrumentId.generate(),
    override val securityListing: Sourced<SecurityListing>,
    override val incomeHistory: HistoricalData<IncomePayment>,
) : MarketInstrument
