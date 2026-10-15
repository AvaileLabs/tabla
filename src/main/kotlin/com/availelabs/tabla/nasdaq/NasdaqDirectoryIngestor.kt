package com.availelabs.tabla.nasdaq

import com.availelabs.tabla.nasdaq.persistence.*
import com.availelabs.tabla.nasdaq.persistence.dto.NasdaqDirectoryEntryInput
import com.availelabs.tabla.nasdaq.persistence.dto.NasdaqDirectoryInput
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Component
class NasdaqDirectoryIngestor {
    private val logger = LoggerFactory.getLogger(NasdaqDirectoryIngestor::class.java)
    private val source = URI("https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqtraded.txt")

    @OptIn(ExperimentalUuidApi::class)
    fun ingest(): NasdaqDirectoryEntity {
        try {
            val rawTable = DataFrame.readCsv(
                url = source.toURL(),
                delimiter = '|',
                parserOptions = ParserOptions(nullStrings = setOf(""))
            ).cast<NasdaqTradedSecurity>(verify = false)

            val retrievedAt = Clock.System.now()
            val prefix = "File Creation Time:"
            val directoryId = 1

            val fileCreationTime = rawTable
                .single { nasdaqTraded.startsWith(prefix) }
                .nasdaqTraded.removePrefix(prefix).trim()
                .let {
                    LocalDateTime.Format {
                        monthNumber()
                        day()
                        year()
                        hour()
                        char(':')
                        minute()
                    }.parse(it)
                }

            val entries = rawTable
                .drop { nasdaqTraded.startsWith(prefix) }
                .cast<NasdaqTestIssueSchema>(verify = true)
                .drop { testIssue != 'N' }
                .remove { testIssue }
                .cast<NasdaqTradedSecurity>(verify = true)
                .map { row ->
                    NasdaqDirectoryEntryInput(
                        id = Uuid.generateV7(),
                        directoryId = directoryId,
                        security = NasdaqDirectoryEntryInput.TargetOf_security(
                            nasdaqTraded = row.nasdaqTraded,
                            symbol = row.symbol,
                            securityName = row.securityName,
                            listingExchange = row.listingExchange,
                            marketCategory = row.marketCategory,
                            etf = row.etf,
                            roundLotSize = row.roundLotSize,
                            financialStatus = row.financialStatus,
                            cqsSymbol = row.cqsSymbol,
                            nasdaqSymbol = row.nasdaqSymbol,
                            nextShares = row.nextShares
                        )
                    ).toEntity()
                }

            return NasdaqDirectoryInput(
                id = directoryId,
                sourceUri = source,
                retrievedAt = retrievedAt,
                fileCreatedAt = fileCreationTime
            ).toEntity {
                this.entries = entries
            }
        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
            throw e
        }
    }
}
