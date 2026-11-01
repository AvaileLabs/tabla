package com.availelabs.tabla.market

import kotlin.uuid.Uuid

@JvmInline
value class SecurityListingId private constructor(
    val value: Uuid
) {
    companion object {
        fun generate(): SecurityListingId =
            SecurityListingId(Uuid.generateV7())

        internal fun from(value: Uuid): SecurityListingId =
            SecurityListingId(value)
    }
}

data class SecuritySymbols private constructor(
    val symbol: String,
    val nasdaqSymbol: String,
    val cqsSymbol: String?,
    val edgarSymbol: String
) {
    init {
        require(symbol.isNormalizedSymbol()) {
            "Symbol must be non-empty and normalized"
        }
        require(nasdaqSymbol.isNormalizedSymbol()) {
            "NASDAQ symbol must be non-empty and normalized"
        }
        require(cqsSymbol == null || cqsSymbol.isNormalizedSymbol()) {
            "CQS symbol must be normalized when present"
        }
        require(edgarSymbol.isNormalizedSymbol()) {
            "EDGAR symbol must be non-empty and normalized"
        }
    }

    companion object {
        fun of(
            symbol: String,
            nasdaqSymbol: String,
            cqsSymbol: String?,
            edgarSymbol: String
        ): SecuritySymbols =
            SecuritySymbols(
                symbol = symbol.trim().uppercase(),
                nasdaqSymbol = nasdaqSymbol.trim().uppercase(),
                cqsSymbol = cqsSymbol
                    ?.trim()
                    ?.uppercase()
                    ?.takeIf { it.isNotEmpty() },
                edgarSymbol = edgarSymbol.trim().uppercase()
            )
    }
}

data class SecurityListingData private constructor(
    val cik: Int,
    val symbols: SecuritySymbols,
    val name: String,
    val exchange: Exchange
) {
    init {
        require(cik > 0) {
            "CIK must be a positive integer"
        }
        require(name.isNotBlank() && name == name.trim()) {
            "Name must be non-empty and trimmed"
        }
    }

    companion object {
        fun of(
            cik: Int,
            symbols: SecuritySymbols,
            name: String,
            exchange: Exchange
        ): SecurityListingData =
            SecurityListingData(
                cik = cik,
                symbols = symbols,
                name = name.trim(),
                exchange = exchange
            )
    }
}

class SecurityListing private constructor(
    val id: SecurityListingId,
    val data: SecurityListingData
) {
    val cik: Int get() = data.cik
    val symbols: SecuritySymbols get() = data.symbols
    val name: String get() = data.name
    val exchange: Exchange get() = data.exchange

    override fun equals(other: Any?): Boolean =
        this === other ||
                (other is SecurityListing && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "SecurityListing(id=$id, data=$data)"

    companion object {
        fun of(
            id: SecurityListingId,
            data: SecurityListingData
        ): SecurityListing =
            SecurityListing(id, data)
    }
}

private fun String.isNormalizedSymbol(): Boolean =
    isNotEmpty() && this == trim().uppercase()

enum class Exchange {
    NASDAQ,
    NYSE_AMERICAN,
    NYSE,
    NYSE_ARCA,
    CBOE_BZX,
    IEX,
    TXSE,
    NYSE_TEXAS
}
