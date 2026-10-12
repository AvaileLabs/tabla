package com.availelabs.tabla.securities

import com.availelabs.tabla.Exchange
import com.availelabs.tabla.SourceInfo
import com.availelabs.tabla.SourceTimestamp
import com.availelabs.tabla.securities.persistence.*
import com.availelabs.tabla.securities.persistence.dto.NasdaqDirectoryEntryInput
import com.availelabs.tabla.securities.persistence.dto.NasdaqDirectoryInput
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Component
class NasdaqSecurityDirectoryIngestor {
    private val logger = LoggerFactory.getLogger(NasdaqSecurityDirectoryIngestor::class.java)
    private val nasdaqTradedSecuritiesSource = URI("https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqtraded.txt")

    fun ingest(): NasdaqDirectoryEntity {
        try {
            val markEmptyFieldsAsNull = ParserOptions(nullStrings = setOf(""))

            val rawTable = readListingTable(nasdaqTradedSecuritiesSource, markEmptyFieldsAsNull)
            val parsedTable = ParsedListingTable.parse(rawTable)

            return parsedTable.toDirectoryEntity()
        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
            throw e
        }
    }

    private data class RetrievedListingTable(
        val value: DataFrame<NasdaqTradedSecurity>,
        val uri: URI,
        val retrievedAt: Instant
    )

    private class ParsedListingTable private constructor(
        val value: DataFrame<NasdaqTradedSecurity>,
        uri: URI,
        retrievedAt: Instant,
        val fileCreationTime: LocalDateTime
    ) {
        val sourceInfo = SourceInfo(
            uri = uri,
            retrievedAt = retrievedAt,
            createdAt = SourceTimestamp.DateTime(fileCreationTime)
        )

        companion object {
            fun parse(rawTable: RetrievedListingTable): ParsedListingTable {
                val [normalizedTable, fileCreationTime] = normalizeListingTable(rawTable.value)

                return ParsedListingTable(
                    value = normalizedTable.cast<NasdaqTradedSecurity>(verify = true),
                    uri = rawTable.uri,
                    retrievedAt = rawTable.retrievedAt,
                    fileCreationTime = fileCreationTime
                )
            }

            private fun normalizeListingTable(
                rawTable: DataFrame<NasdaqTradedSecurity>
            ): Pair<DataFrame<NasdaqTradedSecurity>, LocalDateTime> {
                val prefix = "File Creation Time:"

                val rawFileCreationTime = rawTable
                    .single { nasdaqTraded.startsWith(prefix) }
                    .nasdaqTraded.removePrefix(prefix).trim()
                val fileCreationTime = parseCreationTime(rawFileCreationTime)

                val normalizedTable = rawTable
                    .drop { nasdaqTraded.startsWith(prefix) }
                    .cast<NasdaqTestIssueSchema>(verify = true)
                    .drop { testIssue != 'N' }
                    .remove { testIssue }
                    .cast<NasdaqTradedSecurity>(verify = true)

                return normalizedTable to fileCreationTime
            }

            private fun parseCreationTime(creationTime: String): LocalDateTime {
                val format = LocalDateTime.Format {
                    monthNumber()
                    day()
                    year()
                    hour()
                    char(':')
                    minute()
                }

                return format.parse(creationTime)
            }
        }
    }

    private fun readListingTable(source: URI, parserOptions: ParserOptions): RetrievedListingTable {
        val rawTable = DataFrame.readCsv(
            url = source.toURL(),
            delimiter = '|',
            parserOptions = parserOptions,
        )
        return RetrievedListingTable(
            value = rawTable.cast<NasdaqTradedSecurity>(verify = false),
            uri = source,
            retrievedAt = Clock.System.now()
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun ParsedListingTable.toDirectoryEntity(): NasdaqDirectoryEntity {
        val entries = value.map { row ->
            NasdaqDirectoryEntryInput(
                id = Uuid.generateV7(),
                directoryId = 1,
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
            id = 1,
            sourceUri = sourceInfo.uri.toString(),
            retrievedAt = sourceInfo.retrievedAt,
            fileCreatedAt = fileCreationTime
        ).toEntity {
            this.entries = entries
        }
    }

    private fun exchangeOf(code: Char): Exchange =
        when (code) {
            'A' -> Exchange.NYSE_AMERICAN
            'F' -> Exchange.TXSE
            'M' -> Exchange.NYSE_TEXAS
            'N' -> Exchange.NYSE
            'P' -> Exchange.NYSE_ARCA
            'Q' -> Exchange.NASDAQ
            'Z' -> Exchange.CBOE_BZX
            'V' -> Exchange.IEX
            else -> error("Unsupported exchange code: $code")
        }
}