package com.availelabs.tabla.ingestion

import com.availelabs.tabla.ingestion.snapshot.SourceSnapshotRepository
import com.availelabs.tabla.ingestion.source.SnapshotSource
import com.availelabs.tabla.ingestion.source.SourceCatalog
import com.availelabs.tabla.ingestion.source.SourceFile
import com.availelabs.tabla.ingestion.source.sourcedBy
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import kotlin.time.Clock

@TestConfiguration(proxyBeanMethods = false)
class SnapshotFixturesConfiguration {

    // Seed the real database before the startup ApplicationRunner executes.
    @Bean
    fun sourceSnapshotFixtures(repository: SourceSnapshotRepository): InitializingBean =
        InitializingBean {
            save(repository, SourceCatalog.nasdaqDirectory, "nasdaqtraded.txt")
            save(repository, SourceCatalog.edgarCompanyTickers, "company_tickers_exchange.json")
            save(repository, SourceCatalog.edgarMutualFundTickers, "company_tickers_mf.json")
        }

    private fun <T : SourceFile> save(
        repository: SourceSnapshotRepository,
        source: SnapshotSource<T>,
        filename: String
    ) {
        val content = checkNotNull(javaClass.getResourceAsStream("/ingestion/$filename"))
            .use { it.readBytes() }
        repository.save(source, content.sourcedBy(source, Clock.System.now()))
    }
}
