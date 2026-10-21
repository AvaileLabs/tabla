package com.availelabs.tabla.ingestion.edgar

import com.availelabs.tabla.ingestion.source.EdgarCompanyTickersRawFile
import com.availelabs.tabla.ingestion.source.EdgarFundTickersRawFile
import com.availelabs.tabla.ingestion.source.SnapshotSource
import com.availelabs.tabla.ingestion.source.SourceCatalog
import com.availelabs.tabla.ingestion.source.SourceFile
import com.availelabs.tabla.ingestion.source.sourcedBy
import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.springframework.http.HttpHeaders
import java.io.ByteArrayInputStream
import kotlin.time.Clock

class EdgarTickerMappingIngestor(
    private val properties: EdgarProperties
) {
    val companySource = SourceCatalog.edgarCompanyTickers
    val fundSource = SourceCatalog.edgarMutualFundTickers

    fun downloadCompanies(): Sourced<EdgarCompanyTickersRawFile> = download(companySource)
    fun downloadFunds(): Sourced<EdgarFundTickersRawFile> = download(fundSource)

    private fun <T : SourceFile> download(source: SnapshotSource<T>): Sourced<T> {
        val connection = source.uri.toURL().openConnection().apply {
            setRequestProperty(HttpHeaders.USER_AGENT, properties.userAgent)
        }

        val bytes = connection.getInputStream().use { it.readBytes() }
        return bytes.sourcedBy(source = source, retrievedAt = Clock.System.now())
    }

    fun ingestCompanies(file: Sourced<EdgarCompanyTickersRawFile>): Sourced<DataFrame<EdgarCompanyTickerMapping>> =
        parseResponse(file.value.bytes)
            .decodeRows(EdgarCompanyTickerMapping.serializer())
            .toDataFrame()
            .cast<EdgarCompanyTickerMapping>(verify = true)
            .sourcedBy(uri = file.sourceInfo.uri, retrievedAt = file.sourceInfo.retrievedAt)

    fun ingestMutualFunds(file: Sourced<EdgarFundTickersRawFile>): Sourced<DataFrame<EdgarMutualFundTickerMapping>> =
        parseResponse(file.value.bytes)
            .decodeRows(EdgarMutualFundTickerMapping.serializer())
            .toDataFrame()
            .cast<EdgarMutualFundTickerMapping>(verify = true)
            .sourcedBy(uri = file.sourceInfo.uri, retrievedAt = file.sourceInfo.retrievedAt)

    @OptIn(ExperimentalSerializationApi::class)
    private fun parseResponse(snapshot: ByteArray): SecTickerResponse =
        ByteArrayInputStream(snapshot).use {
            Json.decodeFromStream<SecTickerResponse>(it)
        }
}


private fun <T : Any> SecTickerResponse.decodeRows(
    serializer: KSerializer<T>
): List<T> {
    val expectedFields = serializer.descriptor.elementNames.toList()

    require(fields == expectedFields) {
        "Unexpected SEC ticker fields: $fields"
    }

    return data.map { values ->
        require(values.size == fields.size) {
            "Unexpected row length: ${values.size}"
        }

        val json = JsonObject(fields.zip(values).toMap())

        Json.decodeFromJsonElement(serializer, json)
    }
}

@Serializable
@DataSchema
data class EdgarCompanyTickerMapping(
    val cik: Int,
    val name: String,
    val ticker: String,
    val exchange: String?
)

@Serializable
@DataSchema
data class EdgarMutualFundTickerMapping(
    val cik: Int,
    val seriesId: String,
    val classId: String,
    val symbol: String
)

@Serializable
private data class SecTickerResponse(
    val fields: List<String>,
    val data: List<List<JsonElement>>
)
