package com.availelabs.tabla.market

import com.availelabs.tabla.ApplicationLifecycle
import com.availelabs.tabla.PostgresTestConfiguration
import com.availelabs.tabla.market.internal.SecurityListingEntity
import kotlinx.collections.immutable.persistentListOf
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@ApplicationModuleTest(extraIncludes = ["persistence"])
@Import(PostgresTestConfiguration::class)
@TestPropertySource(properties = ["tabla.edgar.user-agent=Tabla tests test@example.com"])
@Transactional
class SecurityListingDataRepositoryTests {
    @Autowired
    private lateinit var repository: SecurityListingDataRepository

    @Autowired
    private lateinit var sqlClient: KSqlClient

    @MockitoBean(enforceOverride = true)
    private lateinit var lifecycle: ApplicationLifecycle

    @Test
    fun `persists the EDGAR ticker separately from NASDAQ symbols`() {
        val listing = SecurityListing.of(
            cik = 123456,
            symbols = SecuritySymbols("ACMEPA", "ACME-A", "ACMEpA", "ACME-PA"),
            name = "Acme Preferred Stock",
            exchange = Exchange.NYSE
        )

        repository.saveAll(persistentListOf(listing))

        val saved = sqlClient.createQuery(SecurityListingEntity::class) {
            select(table)
        }.execute().single()

        assertEquals(listing.cik, saved.cik)
        assertEquals(listing.symbols.symbol, saved.symbol)
        assertEquals(listing.symbols.nasdaqSymbol, saved.nasdaqSymbol)
        assertEquals(listing.symbols.cqsSymbol, saved.cqsSymbol)
        assertEquals(listing.symbols.edgarSymbol, saved.edgarSymbol)
        assertEquals(listing.exchange, saved.exchange)
    }
}
