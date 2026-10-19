package com.availelabs.tabla.nasdaq

import com.availelabs.tabla.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import kotlin.time.Clock

@Component
class NasdaqDirectoryIngestor {
    private val logger = LoggerFactory.getLogger(NasdaqDirectoryIngestor::class.java)
    val source = SourceCatalog.nasdaqDirectory

    fun download(): Sourced<NasdaqDirectoryRawFile> {
        val bytes = source.uri.toURL().openStream().use {
            it.readBytes()
        }

        return bytes.sourcedBy(
            source = source,
            retrievedAt = Clock.System.now()
        )
    }

    fun parse(file: Sourced<NasdaqDirectoryRawFile>): Sourced<DataFrame<NasdaqTradedSecurity>> {
        val rawTable = DataFrame.readCsv(
            inputStream = ByteArrayInputStream(file.value.bytes),
            delimiter = '|',
            parserOptions = ParserOptions(nullStrings = setOf(""))
        ).cast<NasdaqTradedSecurity>(verify = false)

        val prefix = "File Creation Time:"
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

        val df = rawTable
            .drop { nasdaqTraded.startsWith(prefix) }
            .cast<NasdaqTestIssueSchema>(verify = true)
            .drop { testIssue != 'N' }
            .remove { testIssue }
            .cast<NasdaqTradedSecurity>(verify = true)

        return df.sourcedBy(
            uri = file.sourceInfo.uri,
            retrievedAt = file.sourceInfo.retrievedAt,
            createdAt = SourceTimestamp.DateTime(fileCreationTime)
        )
    }
}
