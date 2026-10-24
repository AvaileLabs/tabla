package com.availelabs.tabla.ingestion

import com.availelabs.tabla.ApplicationLifecycle
import com.availelabs.tabla.PostgresTestConfiguration
import com.availelabs.tabla.ingestion.edgar.EdgarTickerMappingIngestor
import com.availelabs.tabla.ingestion.nasdaq.NasdaqDirectoryIngestor
import com.availelabs.tabla.ingestion.snapshot.SourceSnapshotRepository
import com.availelabs.tabla.ingestion.source.SnapshotSource
import com.availelabs.tabla.ingestion.source.SourceCatalog
import com.availelabs.tabla.ingestion.source.SourceFile
import com.availelabs.tabla.ingestion.source.sourcedBy
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.modulith.test.ApplicationModuleTest
import org.springframework.scheduling.TaskScheduler
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.toJavaInstant
import java.time.Instant as JavaInstant

@ApplicationModuleTest(extraIncludes = ["persistence"])
@Import(PostgresTestConfiguration::class)
@TestPropertySource(properties = ["tabla.edgar.user-agent=Tabla tests test@example.com"])
@Transactional
@Suppress("RETURN_VALUE_NOT_USED")
class IngestionModuleTests {
    @Autowired
    private lateinit var jobs: IngestionJobs

    @MockitoBean(enforceOverride = true)
    private lateinit var lifecycle: ApplicationLifecycle

    @MockitoSpyBean
    private lateinit var repository: SourceSnapshotRepository

    @MockitoSpyBean
    private lateinit var nasdaq: NasdaqDirectoryIngestor

    @MockitoSpyBean
    private lateinit var edgar: EdgarTickerMappingIngestor

    @MockitoSpyBean
    private lateinit var taskScheduler: TaskScheduler

    private val nasdaqContent = """
        Nasdaq Traded|Symbol|Security Name|Listing Exchange|Market Category|ETF|Round Lot Size|Test Issue|Financial Status|CQS Symbol|NASDAQ Symbol|NextShares
        Y|ACME|Acme Holdings|Q|G|N|100|N|N|ACME|ACME|N
        Y|OTHER|Other Holdings|N||N|100|N|||OTHER|N
        File Creation Time: 1023202606:00|||||||||||
    """.trimIndent().encodeToByteArray()

    private val companyContent = """
        {"fields":["cik","name","ticker","exchange"],"data":[[123456,"Acme Holdings","ACME","Nasdaq"],[654321,"Other Holdings","OTHER",null]]}
    """.trimIndent().encodeToByteArray()

    private val fundContent = """
        {"fields":["cik","seriesId","classId","symbol"],"data":[[123456,"S1","C1","ACMEX"]]}
    """.trimIndent().encodeToByteArray()

    @BeforeEach
    fun stubDownloads() {
        val now = Clock.System.now()
        doReturn(nasdaqContent.sourcedBy(SourceCatalog.nasdaqDirectory, now)).`when`(nasdaq).download()
        doReturn(companyContent.sourcedBy(SourceCatalog.edgarCompanyTickers, now)).`when`(edgar).downloadCompanies()
        doReturn(fundContent.sourcedBy(SourceCatalog.edgarMutualFundTickers, now)).`when`(edgar).downloadFunds()
    }

    @Test
    fun `downloads all sources in order and reuses today's snapshots on the next run`() {
        jobs.ingestNow()
        jobs.ingestNow()

        val order = inOrder(nasdaq, edgar, repository)
        order.verify(nasdaq).download()
        order.verify(edgar).downloadCompanies()
        order.verify(edgar).downloadFunds()
        order.verify(repository).latestOf(SourceCatalog.nasdaqDirectory)
        order.verify(repository).latestOf(SourceCatalog.edgarCompanyTickers)

        verify(nasdaq).download()
        verify(edgar).downloadCompanies()
        verify(edgar).downloadFunds()
        verify(repository, times(2)).latestOf(SourceCatalog.nasdaqDirectory)
        verify(repository, times(2)).latestOf(SourceCatalog.edgarCompanyTickers)

        assertSnapshot(SourceCatalog.nasdaqDirectory, nasdaqContent)
        assertSnapshot(SourceCatalog.edgarCompanyTickers, companyContent)
        assertSnapshot(SourceCatalog.edgarMutualFundTickers, fundContent)
    }

    @Test
    fun `schedules the same pipeline between midnight and five in New York`() {
        doReturn(null).`when`(taskScheduler).schedule(any(Runnable::class.java), any(JavaInstant::class.java))
        val today = Clock.System.todayIn(APP_TIME_ZONE)

        jobs.scheduleIngestionForToday()

        val task = ArgumentCaptor.forClass(Runnable::class.java)
        val scheduledAt = ArgumentCaptor.forClass(JavaInstant::class.java)
        verify(taskScheduler).schedule(task.capture(), scheduledAt.capture())
        assertTrue(scheduledAt.value >= today.atStartOfDayIn(APP_TIME_ZONE).toJavaInstant())
        assertTrue(scheduledAt.value < today.atTime(5, 0).toInstant(APP_TIME_ZONE).toJavaInstant())
        verify(nasdaq, never()).download()
        verify(edgar, never()).downloadCompanies()
        verify(edgar, never()).downloadFunds()

        task.value.run()

        assertSnapshot(SourceCatalog.nasdaqDirectory, nasdaqContent)
        assertSnapshot(SourceCatalog.edgarCompanyTickers, companyContent)
        assertSnapshot(SourceCatalog.edgarMutualFundTickers, fundContent)
    }

    @Test
    fun `retention keeps the newest snapshots according to each source policy`() {
        saveHistory(SourceCatalog.nasdaqDirectory, 367)
        saveHistory(SourceCatalog.edgarCompanyTickers, 12)
        saveHistory(SourceCatalog.edgarMutualFundTickers, 12)

        jobs.retainSnapshots()

        assertSnapshot(SourceCatalog.nasdaqDirectory, "snapshot 366".encodeToByteArray(), count = 365)
        assertSnapshot(SourceCatalog.edgarCompanyTickers, "snapshot 11".encodeToByteArray(), count = 10)
        assertSnapshot(SourceCatalog.edgarMutualFundTickers, "snapshot 11".encodeToByteArray(), count = 10)
    }

    private fun <T : SourceFile> saveHistory(source: SnapshotSource<T>, count: Int) {
        val now = Clock.System.now()
        repeat(count) { index ->
            repository.save(
                source,
                "snapshot $index".encodeToByteArray().sourcedBy(source, now - (count - index).days)
            )
        }
    }

    private fun <T : SourceFile> assertSnapshot(source: SnapshotSource<T>, content: ByteArray, count: Long = 1) {
        assertEquals(count, repository.countOf(source.uri))
        assertContentEquals(content, checkNotNull(repository.latestOf(source)).value.bytes)
    }
}
