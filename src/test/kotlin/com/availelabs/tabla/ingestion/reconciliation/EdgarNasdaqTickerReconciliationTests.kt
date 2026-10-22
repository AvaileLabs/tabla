package com.availelabs.tabla.ingestion.reconciliation

import com.availelabs.tabla.ingestion.edgar.EdgarCompanyTickerMapping
import com.availelabs.tabla.ingestion.nasdaq.NasdaqTradedSecurity
import com.availelabs.tabla.ingestion.source.SourceCatalog
import com.availelabs.tabla.market.Exchange
import com.availelabs.tabla.market.SecuritySymbols
import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class EdgarNasdaqTickerReconciliationTests {
    private val reconciliation = EdgarNasdaqTickerReconciliation()
    private val retrievedAt = Instant.parse("2026-10-22T00:00:00Z")

    @Test
    fun `uses NASDAQ listing details and EDGAR CIK when exchanges disagree`() {
        val listings = reconciliation.reconcile(
            nasdaq(),
            edgar(EdgarCompanyTickerMapping(123456, "Acme Holdings", "ACME-A", "Nasdaq"))
        )

        val listing = listings.single()
        assertEquals(123456, listing.cik)
        assertEquals(Exchange.NYSE, listing.exchange)
        assertEquals("Acme Preferred Stock", listing.name)
        assertEquals(SecuritySymbols("ACMEPA", "ACME-A", "ACMEpA"), listing.symbols)
    }

    @Test
    fun `multiple aliases for the same CIK produce one listing`() {
        val listings = reconciliation.reconcile(
            nasdaq(),
            edgar(
                EdgarCompanyTickerMapping(123456, "Acme Holdings", "ACMEpA", "NYSE"),
                EdgarCompanyTickerMapping(123456, "Acme Holdings", "ACME-A", "NYSE")
            )
        )

        assertEquals(123456, listings.single().cik)
    }

    @Test
    fun `conflicting CIKs for the same security are rejected`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            reconciliation.reconcile(
                nasdaq(),
                edgar(
                    EdgarCompanyTickerMapping(123456, "Acme Holdings", "ACMEpA", "NYSE"),
                    EdgarCompanyTickerMapping(654321, "Another Issuer", "ACME-A", "Nasdaq")
                )
            )
        }

        assertEquals("Conflicting EDGAR CIKs for ACMEpA: [123456, 654321]", exception.message)
    }

    private fun nasdaq(): Sourced<DataFrame<NasdaqTradedSecurity>> =
        dataFrameOf(
            "Nasdaq Traded", "Symbol", "Security Name", "Listing Exchange", "Market Category", "ETF",
            "Round Lot Size", "Financial Status", "CQS Symbol", "NASDAQ Symbol", "NextShares"
        )(
            "Y", "ACMEpA", "Acme Preferred Stock", 'N', 'G', 'N',
            100, 'N', "ACMEpA", "ACME-A", 'N'
        ).cast<NasdaqTradedSecurity>(verify = false)
            .sourcedBy(uri = SourceCatalog.nasdaqDirectory.uri, retrievedAt = retrievedAt)

    private fun edgar(vararg tickers: EdgarCompanyTickerMapping): Sourced<DataFrame<EdgarCompanyTickerMapping>> =
        tickers.toList().toDataFrame()
            .cast<EdgarCompanyTickerMapping>(verify = false)
            .sourcedBy(uri = SourceCatalog.edgarCompanyTickers.uri, retrievedAt = retrievedAt)
}
