package com.availelabs.tabla.edgar

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import java.net.URI

@Component
class EdgarTickerMappingIngestor(
    private val properties: EdgarProperties
) {
    private val logger = LoggerFactory.getLogger(EdgarTickerMappingIngestor::class.java)
    private val companySource = URI("https://www.sec.gov/files/company_tickers_exchange.json")
    private val fundSource = URI("https://www.sec.gov/files/company_tickers_mf.json")

    fun ingest() {
        val companyDf = fetchTickerResponse(companySource)
            .decodeRows(EdgarCompanyTickerMapping.serializer())
            .toDataFrame()
        val fundDf = fetchTickerResponse(fundSource)
            .decodeRows(EdgarMutualFundTickerMapping.serializer())
            .toDataFrame()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun fetchTickerResponse(source: URI): SecTickerResponse {
        val connection = source.toURL().openConnection().apply {
            setRequestProperty(HttpHeaders.USER_AGENT, properties.userAgent)
        }

        return connection.getInputStream().use {
            Json.decodeFromStream<SecTickerResponse>(it)
        }
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
data class SecTickerResponse(
    val fields: List<String>,
    val data: List<List<JsonElement>>
)
