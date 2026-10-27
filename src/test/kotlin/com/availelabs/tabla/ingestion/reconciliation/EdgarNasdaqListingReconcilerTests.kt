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
import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class EdgarNasdaqListingReconcilerTests {
    private val reconciliation = EdgarNasdaqListingReconciler()
    private val retrievedAt = Instant.parse("2026-10-22T00:00:00Z")

    @Test
    fun `uses NASDAQ listing details and EDGAR CIK and ticker when exchanges disagree`() {
        val listings = reconciliation.reconcile(
            nasdaq(),
            edgar(EdgarCompanyTickerMapping(123456, "Acme Holdings", "ACME-A", "Nasdaq"))
        )

        val listing = listings.single()
        assertEquals(123456, listing.cik)
        assertEquals(Exchange.NYSE, listing.exchange)
        assertEquals("Acme Preferred Stock", listing.name)
        assertEquals(SecuritySymbols("ACMEPA", "ACME-A", "ACMEpA", "ACME-A"), listing.symbols)
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
        assertEquals("ACMEpA", listings.single().symbols.edgarSymbol)
    }

    @Test
    fun `EDGAR ticker can match only the CQS symbol`() {
        val listings = reconciliation.reconcile(
            nasdaq(symbol = "ACME"),
            edgar(EdgarCompanyTickerMapping(123456, "Acme Holdings", "ACMEpA", "NYSE"))
        )

        assertEquals(SecuritySymbols("ACME", "ACME-A", "ACMEpA", "ACMEpA"), listings.single().symbols)
    }

    @Test
    fun `conflicting CIKs for the same security are rejected`() {
        val exception = assertFailsWith<IllegalStateException> {
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

    @Test
    fun `unmatched securities are omitted and matching rows retain their order`() {
        val securities = nasdaq(symbol = "OTHER", cqsSymbol = null, nasdaqSymbol = "OTHER").value
            .concat(
                nasdaq(symbol = "MISSING", cqsSymbol = null, nasdaqSymbol = "MISSING").value,
                nasdaq().value,
                nasdaq().value
            )
            .sourcedBy(uri = SourceCatalog.nasdaqDirectory.uri, retrievedAt = retrievedAt)

        val listings = reconciliation.reconcile(
            securities,
            edgar(
                EdgarCompanyTickerMapping(123456, "Acme Holdings", "ACME-A", "NYSE"),
                EdgarCompanyTickerMapping(654321, "Other Issuer", "OTHER", "NYSE")
            )
        )

        assertEquals(listOf("OTHER", "ACMEPA", "ACMEPA"), listings.map { it.symbols.symbol })
        assertEquals(listOf("OTHER", "ACME-A", "ACME-A"), listings.map { it.symbols.edgarSymbol })
        assertEquals(listOf(654321, 123456, 123456), listings.map { it.cik })
    }

    @Test
    fun `ticker matching is case sensitive`() {
        val listings = reconciliation.reconcile(
            nasdaq(),
            edgar(EdgarCompanyTickerMapping(123456, "Acme Holdings", "acme-a", "NYSE"))
        )

        assertEquals(emptyList(), listings)
    }

    @Test
    fun `empty EDGAR data produces no listings`() {
        assertEquals(emptyList(), reconciliation.reconcile(nasdaq(), edgar()))
    }

    @Test
    fun `conflicting CIKs for the same EDGAR ticker are rejected`() {
        val exception = assertFailsWith<IllegalStateException> {
            reconciliation.reconcile(
                nasdaq(),
                edgar(
                    EdgarCompanyTickerMapping(123456, "Acme Holdings", "ACME-A", "NYSE"),
                    EdgarCompanyTickerMapping(654321, "Another Issuer", "ACME-A", "Nasdaq")
                )
            )
        }

        assertEquals("Conflicting EDGAR CIKs for ACMEpA: [123456, 654321]", exception.message)
    }

    private fun nasdaq(
        symbol: String = "ACMEpA",
        cqsSymbol: String? = "ACMEpA",
        nasdaqSymbol: String = "ACME-A"
    ): Sourced<DataFrame<NasdaqTradedSecurity>> =
        dataFrameOf(
            "Nasdaq Traded", "Symbol", "Security Name", "Listing Exchange", "Market Category", "ETF",
            "Round Lot Size", "Financial Status", "CQS Symbol", "NASDAQ Symbol", "NextShares"
        )(
            "Y", symbol, "Acme Preferred Stock", 'N', 'G', 'N',
            100, 'N', cqsSymbol, nasdaqSymbol, 'N'
        ).cast<NasdaqTradedSecurity>(verify = false)
            .sourcedBy(uri = SourceCatalog.nasdaqDirectory.uri, retrievedAt = retrievedAt)

    private fun edgar(vararg tickers: EdgarCompanyTickerMapping): Sourced<DataFrame<EdgarCompanyTickerMapping>> =
        tickers.toList().toDataFrame()
            .cast<EdgarCompanyTickerMapping>(verify = false)
            .sourcedBy(uri = SourceCatalog.edgarCompanyTickers.uri, retrievedAt = retrievedAt)
}
